package it.unibo.sd.project.mastermind.model.mongo;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

import java.util.Objects;

public class DBSingleton {
    public static final String DATABASE_NAME = "huesle-db";
    private static DBSingleton instance;
    private final MongoDatabase database;
    private final MongoClient mongoClient;

    private DBSingleton() {
        String connectionString = Objects.requireNonNull(System.getenv("MONGO_HOST"));
        mongoClient = MongoClients.create(connectionString);
        this.database = mongoClient.getDatabase(DATABASE_NAME);
    }

    public static DBSingleton getInstance() {
        if (instance == null) {
            synchronized (DBSingleton.class) {
                if (instance == null) {
                    instance = new DBSingleton();
                }
            }
        }
        return instance;
    }

    public MongoDatabase getDatabase() {
        return database;
    }

    public MongoDatabase getTestDatabase() {
        return mongoClient.getDatabase(DATABASE_NAME + "-test");
    }
}
