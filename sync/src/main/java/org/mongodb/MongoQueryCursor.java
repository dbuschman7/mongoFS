package org.mongodb;

import org.mongodb.connection.ServerAddress;

import com.mongodb.DBCursor;

public class MongoQueryCursor<T> implements MongoCursor<T> {

    private DBCursor surrogate;

    public MongoQueryCursor(final DBCursor cursor) {
        this.surrogate = cursor;

    }

    @SuppressWarnings("unchecked")
    public T next() {
        return (T) new Document(surrogate.next());
    }

    public boolean hasNext() {
        return surrogate.hasNext();
    }

    @Override
    public void close() {

        surrogate.close();
    }

    @Override
    public void remove() {
        surrogate.remove();
    }

    @Override
    public ServerAddress getServerAddress() {

        return new ServerAddress(surrogate.getServerAddress().getHost(), surrogate.getServerAddress().getPort());
    }

    @Override
    public ServerCursor getServerCursor() {
        throw new UnsupportedOperationException();
    }
}
