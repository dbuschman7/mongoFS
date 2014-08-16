package org.mongodb;

import com.mongodb.DBCollection;
import com.mongodb.DBObject;

public class MongoCollection<T> {

    private DBCollection surrogate;

    public MongoCollection(DBCollection db) {
        this.surrogate = db;
    }

    public void save(Document document) {

        DBObject dbObject = document.surrogate;

        Object id = dbObject.get("_id");
        if (id == null) {
            surrogate.insert(dbObject);
        }
        else {
            surrogate.save(dbObject);
        }
    }

    public String getName() {
        return surrogate.getName();
    }

    public MongoDatabase getDatabase() {
        return new MongoDatabase(surrogate.getDB());
    }

    public MongoView<T> find(Document in) {
        return new MongoView<T>(surrogate.find(in.surrogate));
    }

    public CollectionAdministration tools() {
        return new CollectionAdministration(surrogate);
    }

    public WriteResult remove(Document filesQuery) {
        return new WriteResult(surrogate.remove(filesQuery.surrogate));
    }

    public void createIndex(Document document) {
        surrogate.createIndex(document.surrogate);
    }
}
