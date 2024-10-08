package it.unibo.sd.project.mastermind.controllers;

import com.mongodb.client.MongoDatabase;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.JWTOptions;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.authentication.TokenCredentials;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import it.unibo.sd.project.mastermind.controllers.utils.HttpStatusCodes;
import it.unibo.sd.project.mastermind.model.match.Match;
import it.unibo.sd.project.mastermind.model.match.MatchState;
import it.unibo.sd.project.mastermind.model.match.MatchStatus;
import it.unibo.sd.project.mastermind.model.mongo.DBManager;
import it.unibo.sd.project.mastermind.model.request.LoginRequest;
import it.unibo.sd.project.mastermind.model.request.PendingMatchRequest;
import it.unibo.sd.project.mastermind.model.result.UserOperationResult;
import it.unibo.sd.project.mastermind.model.user.Player;
import it.unibo.sd.project.mastermind.presentation.Presentation;
import org.bson.conversions.Bson;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.mongodb.client.model.Filters.elemMatch;
import static com.mongodb.client.model.Filters.eq;

public class UserController {
    private final DBManager<Player> userDB;
    private final DBManager<Match> matchDB;
    private final DBManager<PendingMatchRequest> pendingMatchDB;
    private final String EMAIL_ALREADY_EXISTS_MESSAGE = "The email address is already in use";
    private final String USERNAME_ALREADY_EXISTS_MESSAGE = "The username is already in use";
    private final String UNAUTHORIZED_MESSAGE = "Unauthorized";

    private enum TokenType {
        ACCESS, REFRESH
    }

    public UserController(MongoDatabase database) {
        this.userDB = new DBManager<>(database, "users", "username", Player.class);
        this.matchDB = new DBManager<>(database, "matches", "_id", Match.class);
        this.pendingMatchDB = new DBManager<>(database, "pendingRequests", "requesterUsername",
            PendingMatchRequest.class);
    }

    public Function<String, String> registerUser() {
        return (message) -> {
            UserOperationResult registrationResult = null;
            try {
                Player newUser = Presentation.deserializeAs(message, Player.class);
                if (userDB.isPresentByField("email", newUser.getEmail()))
                    registrationResult =
                        new UserOperationResult(HttpStatusCodes.CONFLICT, EMAIL_ALREADY_EXISTS_MESSAGE);
                else if (userDB.isPresentByField("username", newUser.getUsername()))
                    registrationResult =
                        new UserOperationResult(HttpStatusCodes.CONFLICT, USERNAME_ALREADY_EXISTS_MESSAGE);

                if (registrationResult == null) {
                    // The registration process can go on without problems
                    userDB.insert(newUser);
                    registrationResult = new UserOperationResult(
                        HttpStatusCodes.CREATED,
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
                    userToLogin.get().verifyPassword(loginRequest.getClearPassword())) {
                    Player user = userToLogin.get();

                    // generate access and refresh tokens
                    String accessToken = generateToken(TokenType.ACCESS, user.getUsername());
                    String refreshToken = generateToken(TokenType.REFRESH, user.getUsername());

                    // Save the refresh token in the database
                    user.setRefreshToken(refreshToken);
                    userDB.update(user.getUsername(), user);

                    loginResult = new UserOperationResult(
                        HttpStatusCodes.OK,
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
                    loginResult = new UserOperationResult(HttpStatusCodes.UNAUTHORIZED, UNAUTHORIZED_MESSAGE);
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
                        HttpStatusCodes.OK,
                        "The user " + player.getUsername() + " is successfully logged out");
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            } finally {
                if (result == null)
                    result = new UserOperationResult(
                        HttpStatusCodes.NO_CONTENT,
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
                                HttpStatusCodes.OK,
                                "Access token refreshed successfully",
                                player, newAccessToken));
                        }
                    });
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            } finally {
                if (result.get() == null)
                    result.set(new UserOperationResult(HttpStatusCodes.FORBIDDEN, "Forbidden"));
            }
            return Presentation.serializerOf(UserOperationResult.class).serialize(result.get());
        };
    }

    public Function<String, String> deleteUserAccount() {
        return username -> {
            UserOperationResult result = null;
            try {
                Optional<Player> optionalPlayer = userDB.getDocumentByField("username", username);
                if (optionalPlayer.isPresent()) {
                    // leave all matches the user was playing
                    Bson matchesOfUserQuery = elemMatch("matchStatus.players", eq("username", username));
                    matchDB.getDocumentsByQuery(matchesOfUserQuery)
                        .ifPresent(matches ->
                            matches.stream()
                                .filter(match -> match.getMatchStatus().getState() == MatchState.PLAYING)
                                .forEach(playingMatch -> {
                                    MatchStatus matchStatus = playingMatch.getMatchStatus();
                                    matchStatus.setState(MatchState.VICTORY);
                                    Stream<Player> otherPlayer = matchStatus
                                        .getPlayers()
                                        .stream()
                                        .filter(player -> !player.getUsername().equals(username));
                                    otherPlayer.findFirst()
                                        .ifPresent(p -> matchStatus.changeNextPlayer(p.getUsername()));
                                    matchStatus.setAbandoned();
                                    matchDB.update(playingMatch.getMatchID().toString(), playingMatch);
                                })
                        );

                    // delete possible pending request the user has created
                    Bson pendingReqOfCurrentPlayer = eq("requesterUsername", username);
                    pendingMatchDB.getDocumentByQuery(pendingReqOfCurrentPlayer)
                        .ifPresent(pendingMatchRequest -> pendingMatchDB.deleteByQuery(pendingReqOfCurrentPlayer));

                    // disable the user account
                    Player deletedUser = optionalPlayer.get();
                    deletedUser.disable();
                    userDB.update(username, deletedUser);
                    result = new UserOperationResult(
                        HttpStatusCodes.OK, "User " + username + " deleted successfully!"
                    );
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            } finally {
                if (result == null)
                    result = new UserOperationResult(
                        HttpStatusCodes.BAD_REQUEST, "Problems in deleting the user " + username
                    );
            }
            return Presentation.serializerOf(UserOperationResult.class).serialize(result);
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
