package org.mongodb;

import org.bson.BSONObject;

/**
 * A general exception raised in Mongo
 * 
 * @author antoine
 */
public class MongoException extends RuntimeException {

    private static final long serialVersionUID = -4415279469780082174L;

    /**
     * @param msg
     *            the message
     */
    public MongoException(final String msg) {
        super(msg);
        this.code = -3;
    }

    /**
     * 
     * @param code
     *            the error code
     * @param msg
     *            the message
     */
    public MongoException(final int code, final String msg) {
        super(msg);
        this.code = code;
    }

    /**
     * 
     * @param msg
     *            the message
     * @param t
     *            the throwable cause
     */
    public MongoException(final String msg, final Throwable t) {
        super(msg, t);
        this.code = -4;
    }

    /**
     * 
     * @param code
     *            the error code
     * @param msg
     *            the message
     * @param t
     *            the throwable cause
     */
    public MongoException(final int code, final String msg, final Throwable t) {
        super(msg, t);
        this.code = code;
    }

    /**
     * Creates a MongoException from a BSON object representing an error
     * 
     * @param o
     */
    public MongoException(final BSONObject o) {
        this(ServerError.getCode(o), ServerError.getMsg(o, "UNKNOWN"));
    }

    static MongoException parse(final BSONObject o) {
        String s = ServerError.getMsg(o, null);
        if (s == null) {
            return null;
        }
        return new MongoException(ServerError.getCode(o), s);
    }

    /**
     * Gets the exception code
     * 
     * @return code
     */
    public int getCode() {
        return code;
    }

    private final int code;
}
