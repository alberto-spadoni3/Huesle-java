package it.unibo.sd.project.mastermind.model;

import it.unibo.sd.project.mastermind.model.mongo.DBManager;
import it.unibo.sd.project.mastermind.rabbit.MessageType;
import it.unibo.sd.project.mastermind.rabbit.RPCServer;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.function.Function;

public abstract class AbstractManager<T> implements Manager<T> {
    protected DBManager<T> database;

    public void init(String dbCollectionName, Class<T> objectType) {
        database = new DBManager<>("huesle-db", dbCollectionName, "username", objectType);
        Executors.newSingleThreadExecutor().submit(new RPCServer(getManagementCallbacks()));
    }

    protected abstract Map<MessageType, Function<String, String>> getManagementCallbacks();
}
