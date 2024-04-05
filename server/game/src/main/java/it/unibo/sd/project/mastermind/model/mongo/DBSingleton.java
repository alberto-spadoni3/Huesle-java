package it.unibo.sd.project.mastermind.model.mongo;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

import java.util.Objects;

public class DBSingleton {
    public static final String DATABASE_NAME = "huesle-db";
    private final MongoDatabase database;
    private final MongoDatabase testDatabase;

    private static final class InstanceHolder {
        private static final DBSingleton INSTANCE = new DBSingleton();
    }

    private static DBSingleton getInstance() {
        return InstanceHolder.INSTANCE;
    }

    private DBSingleton() {
        String connectionString = Objects.requireNonNull(System.getenv("MONGO_CONN"));
        MongoClient mongoClient = MongoClients.create(connectionString);
        database = mongoClient.getDatabase(DATABASE_NAME);
        testDatabase = mongoClient.getDatabase(DATABASE_NAME + "-test");
    }

    public static MongoDatabase getDatabase() {
        return getInstance().database;
    }

    public static MongoDatabase getTestDatabase() {
        return getInstance().testDatabase;
    }
}
