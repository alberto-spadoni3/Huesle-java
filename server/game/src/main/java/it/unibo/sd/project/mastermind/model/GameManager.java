package it.unibo.sd.project.mastermind.model;

import it.unibo.sd.project.mastermind.model.mongo.DBManager;
import it.unibo.sd.project.mastermind.rabbit.MessageType;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class GameManager extends AbstractManager<Game> {
    private final DBManager<Game> gameDB;

    public GameManager() {
        super.init("matches", Game.class);
        gameDB = super.database;
    }

    @Override
    protected Map<MessageType, Function<String, String>> getManagementCallbacks() {
        Map<MessageType, Function<String, String>> gameCallbacks = new HashMap<>();
        return gameCallbacks;
    }
}
