package org.mongodb;

import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;

public class MongoView<T> {

    private DBCursor surrogate;
    private WriteConcern writeConcern;

    public MongoView(final DBCursor cursor) {
        this.surrogate = cursor;
    }

    public MongoView(final DBCursor cursor, final WriteConcern writeConcern) {
        this.surrogate = cursor;
    }

    public MongoCursor<T> get() {
        return new MongoQueryCursor<T>(surrogate);
    }

    public T getOne() {
        return get().next();
    }

    public MongoView<T> sort(final Document sort) {
        surrogate = surrogate.sort(sort.getSurrogate());
        return this;
    }

    public MongoView<T> withWriteConcern(final WriteConcern writeConcern) {
        this.writeConcern = writeConcern;
        return this;
    }

    public void update(final Document updateQuery) {
        DBObject query = surrogate.getQuery();
        surrogate.getCollection().update(query, updateQuery.getSurrogate(), false, true, writeConcern);
    }

}
