package it.unibo.sd.project.mastermind.model;

import com.mongodb.client.MongoDatabase;
import it.unibo.sd.project.mastermind.model.mongo.DBManager;
import it.unibo.sd.project.mastermind.model.mongo.DBSingleton;
import it.unibo.sd.project.mastermind.rabbit.MessageType;
import it.unibo.sd.project.mastermind.rabbit.RPCServer;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.function.Function;

public abstract class AbstractManager implements Manager {
    protected MongoDatabase database;

    public void init() {
        database = DBSingleton.getInstance().getDatabase();
        Executors.newSingleThreadExecutor().submit(new RPCServer(getManagementCallbacks(), this.getClass().getSimpleName()));
    }

    public void initForTesting() {
        database = DBSingleton.getInstance().getTestDatabase();
        Executors.newSingleThreadExecutor().submit(new RPCServer(getManagementCallbacks(), this.getClass().getSimpleName()));
    }

    protected abstract Map<MessageType, Function<String, String>> getManagementCallbacks();
}
