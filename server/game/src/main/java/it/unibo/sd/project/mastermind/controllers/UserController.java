package it.unibo.sd.project.mastermind.controllers;

import com.mongodb.client.MongoDatabase;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.JWTOptions;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.authentication.TokenCredentials;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import it.unibo.sd.project.mastermind.model.user.Player;
import it.unibo.sd.project.mastermind.model.mongo.DBManager;
import it.unibo.sd.project.mastermind.model.user.LoginRequest;
import it.unibo.sd.project.mastermind.model.user.UserOperationResult;
import it.unibo.sd.project.mastermind.presentation.Presentation;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public class UserController {
    private final DBManager<Player> userDB;
    private final short SUCCESS_HTTP_CODE = 200;
    private final short REGISTRATION_DONE_HTTP_CODE = 201;
    private final short NO_CONTENT_HTTP_CODE = 204;
    private final short UNAUTHORIZED_HTTP_CODE = 401;
    private final short FORBIDDEN_HTTP_CODE = 403;
    private final short CONFLICT_HTTP_CODE = 409;
    private final String EMAIL_ALREADY_EXISTS_MESSAGE = "The email address is already in use";
    private final String USERNAME_ALREADY_EXISTS_MESSAGE = "The username is already in use";
    private final String UNAUTHORIZED_MESSAGE = "Unauthorized";

    private enum TokenType {
        ACCESS, REFRESH
    }

    public UserController(MongoDatabase database) {
        this.userDB = new DBManager<>(database, "users", "username", Player.class);
    }

    public Function<String, String> registerUser() {
        return (message) -> {
            UserOperationResult registrationResult = null;
            try {
                Player newUser = Presentation.deserializeAs(message, Player.class);
                if (userDB.isPresentByField("email", newUser.getEmail()))
                    registrationResult =
                            new UserOperationResult(CONFLICT_HTTP_CODE, EMAIL_ALREADY_EXISTS_MESSAGE);
                else if (userDB.isPresentByField("username", newUser.getUsername()))
                    registrationResult =
                            new UserOperationResult(CONFLICT_HTTP_CODE, USERNAME_ALREADY_EXISTS_MESSAGE);

                if (registrationResult == null) {
                    // The registration process can go on without problems
                    userDB.insert(newUser);
                    registrationResult = new UserOperationResult(
                            REGISTRATION_DONE_HTTP_CODE,
                            String.format("User %s created successfully", newUser.getUsername()));
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            return Presentation.serializerOf(UserOperationResult.class).serialize(registrationResult);
        };
    }

    public Function<String, String> loginUser() {
        return (message) -> {
            UserOperationResult loginResult = null;
            try {
                LoginRequest loginRequest = Presentation.deserializeAs(message, LoginRequest.class);
                Optional<Player> userToLogin =
                        userDB.getDocumentByField(
                                "username",
                                loginRequest.getRequesterUsername()
                        );
                if (userToLogin.isPresent() &&
                        !userToLogin.get().isDisabled() &&
                        userToLogin.get().verifyPassword(loginRequest.getClearPassword()))
                {
                    Player user = userToLogin.get();

                    // generate access and refresh tokens
                    String accessToken = generateToken(TokenType.ACCESS, user.getUsername());
                    String refreshToken = generateToken(TokenType.REFRESH, user.getUsername());

                    // Save the refresh token in the database
                    user.setRefreshToken(refreshToken);
                    userDB.update(user.getUsername(), user);

                    loginResult = new UserOperationResult(
                            SUCCESS_HTTP_CODE,
                            String.format("User %s logged in", userToLogin.get().getUsername()),
                            user, accessToken);
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            } finally {
                if (loginResult == null)
                    // It means that the username is not present in the DB,
                    // or it is disabled
                    // or the passwords doesn't match
                    loginResult = new UserOperationResult(UNAUTHORIZED_HTTP_CODE, UNAUTHORIZED_MESSAGE);
            }
            return Presentation.serializerOf(UserOperationResult.class).serialize(loginResult);
        };
    }

    public Function<String, String> logoutUser() {
        return (refreshToken) -> {
            UserOperationResult result = null;
            try {
                Optional<Player> optionalPlayer =
                        userDB.getDocumentByField("refreshToken", refreshToken);
                if (optionalPlayer.isPresent()) {
                    Player player = optionalPlayer.get();
                    // remove the refreshToken from the database
                    player.setRefreshToken(null);
                    userDB.update(player.getUsername(), player);

                    result = new UserOperationResult(
                            SUCCESS_HTTP_CODE,
                            "The user " + player.getUsername() + " is successfully logged out");
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            } finally {
                if (result == null)
                    result = new UserOperationResult(
                            NO_CONTENT_HTTP_CODE,
                            "The user was already logged out");
            }
            return Presentation.serializerOf(UserOperationResult.class).serialize(result);
        };
    }

    public Function<String, String> refreshAccessToken() {
        return refreshToken -> {
            AtomicReference<UserOperationResult> result = new AtomicReference<>();
            TokenCredentials credentials = new TokenCredentials(refreshToken);
            try {
                Optional<Player> optionalPlayer =
                        userDB.getDocumentByField("refreshToken", credentials.getToken());
                if (optionalPlayer.isPresent()) {
                    Player player = optionalPlayer.get();
                    // check the signature and expiration of the refreshToken
                    getJwtAuthProvider(TokenType.REFRESH).authenticate(credentials, user -> {
                        if (user.succeeded() && user.result().subject().equals(player.getUsername())) {
                            String newAccessToken = generateToken(TokenType.ACCESS, player.getUsername());
                            result.set(new UserOperationResult(
                                    SUCCESS_HTTP_CODE,
                                    "Access token refreshed successfully",
                                    player, newAccessToken));
                        }
                    });
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            } finally {
                if (result.get() == null)
                    result.set(new UserOperationResult(FORBIDDEN_HTTP_CODE, "Forbidden"));
            }
            return Presentation.serializerOf(UserOperationResult.class).serialize(result.get());
        };
    }

    private String generateToken(TokenType tokenType, String username) {
        int TOKEN_EXPIRATION_IN_MINUTES =
                tokenType.equals(TokenType.REFRESH) ?
                        Integer.parseInt(System.getenv("REFRESH_TOKEN_EXPIRATION")) :
                        Integer.parseInt(System.getenv("ACCESS_TOKEN_EXPIRATION"));
        JWTOptions jwtOptions = new JWTOptions().setExpiresInMinutes(TOKEN_EXPIRATION_IN_MINUTES);
        JsonObject tokenData = new JsonObject().put("sub", username);
        JWTAuth jwtAuth = getJwtAuthProvider(tokenType);
        return jwtAuth.generateToken(tokenData, jwtOptions);
    }

    private JWTAuth getJwtAuthProvider(TokenType tokenType) {
        JWTAuthOptions jwtAuthOptions = new JWTAuthOptions()
                .addPubSecKey(new PubSecKeyOptions()
                        .setAlgorithm("HS256")
                        .setBuffer(
                            tokenType.equals(TokenType.ACCESS) ?
                                    System.getenv("ACCESS_TOKEN_SECRET") :
                                    System.getenv("REFRESH_TOKEN_SECRET")
                        ));
        return JWTAuth.create(Vertx.vertx(), jwtAuthOptions);
    }
}
