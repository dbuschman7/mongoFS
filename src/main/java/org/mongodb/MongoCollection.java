package org.mongodb;

import com.mongodb.DBCollection;
import com.mongodb.DBObject;

public class MongoCollection<T> {

    private DBCollection surrogate;

    public MongoCollection(final DBCollection db) {
        this.surrogate = db;
    }

    public void save(final Document document) {

        DBObject dbObject = document.getSurrogate();

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

    public MongoView<T> find(final Document in) {
        return new MongoView<T>(surrogate.find(in.getSurrogate()));
    }

    public CollectionAdministration tools() {
        return new CollectionAdministration(surrogate);
    }

    public WriteResult remove(final Document filesQuery) {
        return new WriteResult(surrogate.remove(filesQuery.getSurrogate()));
    }

    public void createIndex(final Document document) {
        surrogate.createIndex(document.getSurrogate());
    }
}
