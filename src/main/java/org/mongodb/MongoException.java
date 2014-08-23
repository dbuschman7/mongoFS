package org.mongodb;

import org.bson.BSONObject;

public class MongoException extends com.mongodb.MongoException {

    private static final long serialVersionUID = 2144706252609823555L;

    public MongoException(final BSONObject o) {
        super(o);
    }

    public MongoException(final int code, final String msg, final Throwable t) {
        super(code, msg, t);
    }

    public MongoException(final int code, final String msg) {
        super(code, msg);
    }

    public MongoException(final String msg, final Throwable t) {
        super(msg, t);
    }

    public MongoException(final String msg) {
        super(msg);
    }

}
