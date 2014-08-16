package org.mongodb;

import org.bson.BSONObject;

public class MongoException extends com.mongodb.MongoException {

    private static final long serialVersionUID = 2144706252609823555L;

    public MongoException(BSONObject o) {
        super(o);
        // TODO Auto-generated constructor stub
    }

    public MongoException(int code, String msg, Throwable t) {
        super(code, msg, t);
        // TODO Auto-generated constructor stub
    }

    public MongoException(int code, String msg) {
        super(code, msg);
        // TODO Auto-generated constructor stub
    }

    public MongoException(String msg, Throwable t) {
        super(msg, t);
        // TODO Auto-generated constructor stub
    }

    public MongoException(String msg) {
        super(msg);
        // TODO Auto-generated constructor stub
    }

}
