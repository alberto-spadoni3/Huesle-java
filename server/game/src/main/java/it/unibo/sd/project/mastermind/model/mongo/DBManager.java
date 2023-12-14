package it.unibo.sd.project.mastermind.model.mongo;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import it.unibo.sd.project.mastermind.presentation.Presentation;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class DBManager<T> {
    private final static String ID_FIELD = "_id";
    private final MongoDatabase database;
    private final MongoCollection<Document> collection;
    private final Class<T> klass;

    public DBManager(String databaseName, String collectionName, Class<T> klass) {
        String connectionString = Objects.requireNonNull(System.getenv("MONGO_HOST"));
        MongoClient mongoClient = MongoClients.create(connectionString);
        this.database = mongoClient.getDatabase(databaseName);
        this.collection = database.getCollection(collectionName);
        this.klass = klass;
    }

    public void insert(T elem){
        collection.insertOne(convertToDocument(elem));
    }

    public void update(String id, T elem){
        collection.replaceOne(Filters.eq(ID_FIELD, id),convertToDocument(elem));
    }

    public void remove(String id){
        collection.deleteOne(Filters.eq(ID_FIELD, id));
    }

    public boolean isPresentByID(String id){
        return collection.countDocuments(Filters.eq(ID_FIELD, id)) > 0;
    }

    public boolean isPresentByField(String fieldName, String fieldValue){
        return collection.countDocuments(Filters.eq(fieldName, fieldValue)) > 0;
    }

    public Optional<T> getDocumentByField(String fieldName, String fieldValue) throws Exception {
        Document doc = collection.find(Filters.eq(fieldName, fieldValue)).first();
        return doc == null ? Optional.empty() : Optional.of(convertDocumentTo(doc));
    }

    private Document convertToDocument(T elem){
        return Document.parse(Presentation.serializerOf(klass).serialize(elem));
    }

    private T convertDocumentTo(Document d) throws Exception {
        return Presentation.deserializeAs(d.toJson(), klass);
    }
}
