package it.unibo.sd.project.mastermind.model.user;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.JWTOptions;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.authentication.TokenCredentials;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import it.unibo.sd.project.mastermind.model.AbstractManager;
import it.unibo.sd.project.mastermind.model.Player;
import it.unibo.sd.project.mastermind.model.mongo.DBManager;
import it.unibo.sd.project.mastermind.presentation.Presentation;
import it.unibo.sd.project.mastermind.rabbit.MessageType;
import it.unibo.sd.project.mastermind.rabbit.RPCServer;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public class UserManager extends AbstractManager<Player> {
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

    public UserManager() {
        super.init("users", Player.class);
        userDB = super.database;
    }

    @Override
    protected Map<MessageType, Function<String, String>> getManagementCallbacks() {
        Map<MessageType, Function<String, String>> userCallbacks = new HashMap<>();
        userCallbacks.put(MessageType.REGISTER_USER, registerUser());
        userCallbacks.put(MessageType.LOGIN_USER, loginUser());
        userCallbacks.put(MessageType.LOGOUT_USER, logoutUser());
        userCallbacks.put(MessageType.REFRESH_ACCESS_TOKEN, refreshAccessToken());
        return userCallbacks;
    }

    private Function<String, String> registerUser() {
        return (message) -> {
            OperationResult registrationResult = null;
            try {
                Player newUser = Presentation.deserializeAs(message, Player.class);
                if (userDB.isPresentByField("email", newUser.getEmail()))
                    registrationResult =
                            new OperationResult(CONFLICT_HTTP_CODE, EMAIL_ALREADY_EXISTS_MESSAGE);
                else if (userDB.isPresentByField("username", newUser.getUsername()))
                    registrationResult =
                            new OperationResult(CONFLICT_HTTP_CODE, USERNAME_ALREADY_EXISTS_MESSAGE);

                if (registrationResult == null) {
                    // The registration process can go on without problems
                    userDB.insert(newUser);
                    registrationResult = new OperationResult(
                                    REGISTRATION_DONE_HTTP_CODE,
                                    String.format("User %s created successfully", newUser.getUsername()));
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            return Presentation.serializerOf(OperationResult.class).serialize(registrationResult);
        };
    }

    private Function<String, String> loginUser() {
        return (message) -> {
            OperationResult loginResult = null;
            try {
                LoginRequest loginRequest = Presentation.deserializeAs(message, LoginRequest.class);
                Optional<Player> userToLogin =
                        userDB.getDocumentByField(
                                "username",
                                loginRequest.getUsername()
                        );
                if (userToLogin.isPresent() &&
                    !userToLogin.get().isDisabled() &&
                    userToLogin.get().verifyPassword(loginRequest.getClearPassword()))
                {
                    Player user = userToLogin.get();

                    // generate access and refresh tokens
                    String accessToken = generateToken("access.secret", user.getUsername());
                    String refreshToken = generateToken("refresh.secret", user.getUsername());

                    // Save the refresh token in the database
                    user.setRefreshToken(refreshToken);
                    userDB.update(user.getUsername(), user);

                    loginResult = new OperationResult(
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
                    loginResult = new OperationResult(UNAUTHORIZED_HTTP_CODE, UNAUTHORIZED_MESSAGE);
            }
            return Presentation.serializerOf(OperationResult.class).serialize(loginResult);
        };
    }

    private Function<String, String> logoutUser() {
        return (refreshToken) -> {
            OperationResult result = null;
            try {
                Optional<Player> optionalPlayer =
                        userDB.getDocumentByField("refreshToken", refreshToken);
                if (optionalPlayer.isPresent()) {
                    Player player = optionalPlayer.get();
                    // remove the refreshToken from the database
                    player.setRefreshToken(null);
                    userDB.update(player.getUsername(), player);

                    result = new OperationResult(
                            SUCCESS_HTTP_CODE,
                            "The user " + player.getUsername() + " is successfully logged out");
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            } finally {
                if (result == null)
                    result = new OperationResult(NO_CONTENT_HTTP_CODE, "");
            }
            return Presentation.serializerOf(OperationResult.class).serialize(result);
        };
    }

    private Function<String, String> refreshAccessToken() {
        return refreshToken -> {
            AtomicReference<OperationResult> result = new AtomicReference<>();
            TokenCredentials credentials = new TokenCredentials(refreshToken);
            try {
                Optional<Player> optionalPlayer =
                        userDB.getDocumentByField("refreshToken", credentials.getToken());
                if (optionalPlayer.isPresent()) {
                    Player player = optionalPlayer.get();
                    // check the signature and expiration of the refreshToken
                    getJwtAuthProvider("refresh.secret").authenticate(credentials, user -> {
                        if (user.succeeded() && user.result().subject().equals(player.getUsername())) {
                            String newAccessToken = generateToken("access.secret", player.getUsername());
                            result.set(new OperationResult(
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
                    result.set(new OperationResult(FORBIDDEN_HTTP_CODE, "Forbidden"));
            }
            return Presentation.serializerOf(OperationResult.class).serialize(result.get());
        };
    }

    private String generateToken(String tokenSecret, String username) {
        String token;
        final byte TOKEN_EXPIRATION_IN_MINUTES = 2;
        JWTOptions jwtOptions = new JWTOptions()
                .setExpiresInMinutes(TOKEN_EXPIRATION_IN_MINUTES);
        JsonObject tokenData = new JsonObject().put("sub", username);
        JWTAuth jwtAuth = getJwtAuthProvider(tokenSecret);
        token = jwtAuth.generateToken(tokenData, jwtOptions);
        return token;
    }

    private JWTAuth getJwtAuthProvider(String symmetricKey) {
        //TODO: change this password with a random generated string inside an environment variable
        JWTAuthOptions jwtAuthOptions = new JWTAuthOptions()
                .addPubSecKey(new PubSecKeyOptions()
                        .setAlgorithm("HS256")
                        .setBuffer(symmetricKey));
        return JWTAuth.create(Vertx.vertx(), jwtAuthOptions);
    }
}
