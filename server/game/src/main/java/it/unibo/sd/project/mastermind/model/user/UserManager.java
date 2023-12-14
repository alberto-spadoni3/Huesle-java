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

    private final String SUCCESS_HTTP_CODE = "200";
    private final String REGISTRATION_DONE_HTTP_CODE = "201";
    private final String UNAUTHORIZED_HTTP_CODE = "401";
    private final String VALUE_ALREADY_EXISTS_HTTP_CODE = "409";
    private final String EMAIL_ALREADY_EXISTS_MESSAGE = "The email address is already in use";
    private final String USERNAME_ALREADY_EXISTS_MESSAGE = "The username is already in use";

    public UserManager() {
        rpcServer = new RPCServer(getUserManagementCallbacks());
        userDB = new DBManager<>("huesle-db", "users", Player.class);
        this.users = new ArrayList<>();
        // TODO initialize users variable with the elements in DB
        // users.addAll(...)
        try (ExecutorService service = Executors.newSingleThreadExecutor()) {
            System.out.println("Going to start the RPC server...");
            service.submit(rpcServer);
        }
    }

    private Map<MessageType, Function<String, String>> getUserManagementCallbacks() {
        Map<MessageType, Function<String, String>> userCallbacks = new HashMap<>();
        userCallbacks.put(MessageType.REGISTER_USER, registerUser());
        userCallbacks.put(MessageType.LOGIN_USER, loginUser());
        return userCallbacks;
    }

    private Function<String, String> registerUser() {
        return (message) -> {
            String registrationMessage = "";
            try {
                Player newUser = Presentation.deserializeAs(message, Player.class);
                if (userDB.isPresentByField("email", newUser.getEmail()))
                    registrationMessage = VALUE_ALREADY_EXISTS_HTTP_CODE
                                        + "-"
                                        + EMAIL_ALREADY_EXISTS_MESSAGE;
                else if (userDB.isPresentByField("username", newUser.getUsername()))
                    registrationMessage = VALUE_ALREADY_EXISTS_HTTP_CODE
                                        + "-"
                                        + USERNAME_ALREADY_EXISTS_MESSAGE;

                if (registrationMessage.isEmpty()) {
                    // The registration process can go on without problems
                    users.add(newUser);
                    userDB.insert(newUser);
                    registrationMessage = REGISTRATION_DONE_HTTP_CODE
                                        + "-"
                                        + "The user "
                                        + newUser.getUsername()
                                        + " is being registered successfully";
                }

            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            return registrationMessage;
        };
    }

    private Function<String, String> loginUser() {
        // ITER
        // deserializzare e verificare che ci siano tutti i campi
        // verificare che l'utente sia presente nel db
        // verificare che la password fornita corrisponda con quella nel db
        return (message) -> {
            String loginMessage  = "";
            try {
                LoginRequest loginRequest = Presentation.deserializeAs(message, LoginRequest.class);
                Optional<Player> userToLogin =
                        userDB.getDocumentByField(
                                "username",
                                loginRequest.getUsername()
                        );
                if (userToLogin.isPresent() && !userToLogin.get().isDisabled()) {
                    // check password
                    if (userToLogin.get().verifyPassword(loginRequest.getClearPassword()))
                        loginMessage =
                                SUCCESS_HTTP_CODE +
                                "-" +
                                "User logged in";
                    else
                        loginMessage =
                                UNAUTHORIZED_HTTP_CODE +
                                "-" +
                                "Unauthorized";
                } else {
                    // User not in DB or disabled
                    loginMessage =
                            UNAUTHORIZED_HTTP_CODE +
                            "-" +
                            "Unauthorized";
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            return loginMessage;
        };
    }
}
