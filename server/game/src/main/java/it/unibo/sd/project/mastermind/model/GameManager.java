package it.unibo.sd.project.mastermind.model;

import it.unibo.sd.project.mastermind.controllers.GameController;
import it.unibo.sd.project.mastermind.rabbit.MessageType;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class GameManager extends AbstractManager {
    public GameManager() {
        super.init();
    }

    @Override
    protected Map<MessageType, Function<String, String>> getManagementCallbacks() {
        Map<MessageType, Function<String, String>> gameCallbacks = new HashMap<>();
        GameController gameController = new GameController(database);
        gameCallbacks.put(MessageType.SEARCH_MATCH, gameController.searchMatch());
        gameCallbacks.put(MessageType.JOIN, gameController.joinPrivateMatch());
        return gameCallbacks;
    }
}
