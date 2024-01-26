package it.unibo.sd.project.mastermind.model;

import it.unibo.sd.project.mastermind.controllers.GameController;
import it.unibo.sd.project.mastermind.controllers.SettingsController;
import it.unibo.sd.project.mastermind.controllers.StatsController;
import it.unibo.sd.project.mastermind.rabbit.MessageType;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class GameManager extends AbstractManager {
    public GameManager(boolean forTesting) {
        if (forTesting) super.initForTesting();
        else super.init();
    }

    @Override
    protected Map<MessageType, Function<String, String>> getManagementCallbacks() {
        Map<MessageType, Function<String, String>> gameCallbacks = new HashMap<>();

        GameController gameController = new GameController(database);
        gameCallbacks.put(MessageType.SEARCH_MATCH, gameController.searchMatch());
        gameCallbacks.put(MessageType.JOIN_PRIVATE_MATCH, gameController.joinPrivateMatch());
        gameCallbacks.put(MessageType.CANCEL_MATCH_SEARCH, gameController.cancelMatchSearch());
        gameCallbacks.put(MessageType.GET_MATCHES_OF_USER, gameController.getMatchesOfUser());
        gameCallbacks.put(MessageType.GET_MATCH, gameController.getMatchByID());
        gameCallbacks.put(MessageType.LEAVE_MATCH, gameController.leaveMatchByID());
        gameCallbacks.put(MessageType.DO_GUESS, gameController.doGuess());

        SettingsController settingsController = new SettingsController(database);
        gameCallbacks.put(MessageType.GET_SETTINGS, settingsController.getSettings());
        gameCallbacks.put(MessageType.UPDATE_SETTINGS, settingsController.updateAccessibilitySettings());
        gameCallbacks.put(MessageType.UPDATE_PROFILE_PIC, settingsController.updateProfilePictureID());
        gameCallbacks.put(MessageType.UPDATE_EMAIL, settingsController.updateUserEmail());
        gameCallbacks.put(MessageType.UPDATE_PASSWORD, settingsController.updateUserPassword());

        StatsController statsController = new StatsController(database);
        gameCallbacks.put(MessageType.GET_USER_STATS, statsController.getUserStats());
        return gameCallbacks;
    }
}
