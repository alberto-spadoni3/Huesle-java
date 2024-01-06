package it.unibo.sd.project.mastermind.model.mongo;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

import java.util.Objects;

public class DBSingleton {
    private static DBSingleton instance;
    private final MongoDatabase database;

    private DBSingleton() {
        String databaseName = "huesle-db";
        String connectionString = Objects.requireNonNull(System.getenv("MONGO_HOST"));
        MongoClient mongoClient = MongoClients.create(connectionString);
        this.database = mongoClient.getDatabase(databaseName);
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
}
