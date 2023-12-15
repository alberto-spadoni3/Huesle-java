package it.unibo.sd.project.mastermind.model.user;

import it.unibo.sd.project.mastermind.model.Player;
import it.unibo.sd.project.mastermind.model.mongo.DBManager;
import it.unibo.sd.project.mastermind.presentation.Presentation;
import it.unibo.sd.project.mastermind.rabbit.MessageType;
import it.unibo.sd.project.mastermind.rabbit.RPCServer;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

public class UserManager {
    private List<Player> users;
    private final RPCServer rpcServer;
    private final DBManager<Player> userDB;

    private final short SUCCESS_HTTP_CODE = 200;
    private final short REGISTRATION_DONE_HTTP_CODE = 201;
    private final short UNAUTHORIZED_HTTP_CODE = 401;
    private final short VALUE_ALREADY_EXISTS_HTTP_CODE = 409;
    private final String EMAIL_ALREADY_EXISTS_MESSAGE = "The email address is already in use";
    private final String USERNAME_ALREADY_EXISTS_MESSAGE = "The username is already in use";
    private final String UNAUTHORIZED_MESSAGE = "Unauthorized";

    public UserManager() {
        rpcServer = new RPCServer(getUserManagementCallbacks());
        userDB = new DBManager<>("huesle-db", "users", Player.class);
        this.users = new ArrayList<>();
        // TODO initialize users variable with the elements in DB
        // users.addAll(...)
        Executors.newSingleThreadExecutor().submit(rpcServer);
    }

    private Map<MessageType, Function<String, String>> getUserManagementCallbacks() {
        Map<MessageType, Function<String, String>> userCallbacks = new HashMap<>();
        userCallbacks.put(MessageType.REGISTER_USER, registerUser());
        userCallbacks.put(MessageType.LOGIN_USER, loginUser());
        return userCallbacks;
    }

    private Function<String, String> registerUser() {
        return (message) -> {
            OperationResult registrationResult = null;
            try {
                Player newUser = Presentation.deserializeAs(message, Player.class);
                if (userDB.isPresentByField("email", newUser.getEmail()))
                    registrationResult =
                            new OperationResult(VALUE_ALREADY_EXISTS_HTTP_CODE, EMAIL_ALREADY_EXISTS_MESSAGE);
                else if (userDB.isPresentByField("username", newUser.getUsername()))
                    registrationResult =
                            new OperationResult(VALUE_ALREADY_EXISTS_HTTP_CODE, USERNAME_ALREADY_EXISTS_MESSAGE);

                if (registrationResult == null) {
                    // The registration process can go on without problems
                    users.add(newUser);
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
            OperationResult loginResult  = null;
            try {
                LoginRequest loginRequest = Presentation.deserializeAs(message, LoginRequest.class);
                Optional<Player> userToLogin =
                        userDB.getDocumentByField(
                                "username",
                                loginRequest.getUsername()
                        );
                if (userToLogin.isPresent() &&
                    !userToLogin.get().isDisabled() &&
                    userToLogin.get().verifyPassword(loginRequest.getClearPassword())) {
                    loginResult = new OperationResult(
                            SUCCESS_HTTP_CODE,
                            String.format("User %s logged in", userToLogin.get().getUsername()));
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
}
