package it.unibo.sd.project.mastermind.model.mongo;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import it.unibo.sd.project.mastermind.presentation.Presentation;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DBManager<T> {
    private final String ID_FIELD;
    private final MongoCollection<Document> collection;
    private final Class<T> klass;

    public DBManager(MongoDatabase database, String collectionName, String idField, Class<T> klass) {
        this.collection = database.getCollection(collectionName);
        this.ID_FIELD = idField;
        this.klass = klass;
    }

    public void insert(T elem) {
        collection.insertOne(convertToDocument(elem));
    }

    public void update(String id, T elem) {
        collection.replaceOne(Filters.eq(ID_FIELD, id), convertToDocument(elem));
    }

    public void deleteByQuery(Bson query) {
        collection.deleteOne(query);
    }

    public boolean isPresentByID(String id) {
        return collection.countDocuments(Filters.eq(ID_FIELD, id)) > 0;
    }

    public boolean isPresentByField(String fieldName, String fieldValue) {
        return collection.countDocuments(Filters.eq(fieldName, fieldValue)) > 0;
    }

    public Optional<T> getDocumentByField(String fieldName, String fieldValue) throws Exception {
        Document doc = collection.find(Filters.eq(fieldName, fieldValue)).first();

        return doc == null ? Optional.empty() : Optional.of(convertDocumentTo(doc));
    }

    public Optional<T> getDocumentByQuery(Bson query) throws Exception {
        Document doc = collection.find(query).first();
        return doc == null ? Optional.empty() : Optional.of(convertDocumentTo(doc));
    }

    public Optional<List<T>> getDocumentsByQuery(Bson query) throws Exception {
        List<T> foundDocuments = new ArrayList<>();
        try (MongoCursor<Document> cursor = collection.find(query).iterator()) {
            if (!cursor.hasNext())
                return Optional.empty();
            while (cursor.hasNext())
                foundDocuments.add(convertDocumentTo(cursor.next()));
        }
        return Optional.of(foundDocuments);
    }

    private Document convertToDocument(T elem) {
        return Document.parse(Presentation.serializerOf(klass).serialize(elem));
    }

    private T convertDocumentTo(Document d) throws Exception {
        return Presentation.deserializeAs(d.toJson(), klass);
    }
}
