package org.mongodb;

import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;

public class MongoView<T> {

    private DBCursor surrogate;
    private WriteConcern writeConcern;

    public MongoView(DBCursor cursor) {
        this.surrogate = cursor;
    }

    public MongoView(DBCursor cursor, WriteConcern writeConcern) {
        this.surrogate = cursor;
    }

    public MongoCursor<T> get() {
        return new MongoQueryCursor<T>(surrogate);
    }

    public T getOne() {
        return get().next();
    }

    public MongoView<T> sort(Document sort) {
        surrogate = surrogate.sort(sort.surrogate);
        return this;
    }

    public MongoView<T> withWriteConcern(WriteConcern writeConcern) {
        this.writeConcern = writeConcern;
        return this;
    }

    public void update(Document updateQuery) {
        DBObject query = surrogate.getQuery();
        surrogate.getCollection().update(query, updateQuery.surrogate, false, true);
    }

}
