package it.unibo.sd.project.mastermind.model.user;

import it.unibo.sd.project.mastermind.controllers.UserController;
import it.unibo.sd.project.mastermind.model.AbstractManager;
import it.unibo.sd.project.mastermind.rabbit.MessageType;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class UserManager extends AbstractManager {
    public UserManager(boolean forTesting) {
        if (forTesting) super.initForTesting();
        else super.init();
    }

    @Override
    protected Map<MessageType, Function<String, String>> getManagementCallbacks() {
        Map<MessageType, Function<String, String>> userCallbacks = new HashMap<>();
        UserController userController = new UserController(database);
        userCallbacks.put(MessageType.REGISTER_USER, userController.registerUser());
        userCallbacks.put(MessageType.LOGIN_USER, userController.loginUser());
        userCallbacks.put(MessageType.LOGOUT_USER, userController.logoutUser());
        userCallbacks.put(MessageType.REFRESH_ACCESS_TOKEN, userController.refreshAccessToken());
        return userCallbacks;
    }
}
