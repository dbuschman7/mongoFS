package me.lightspeed7.mongofs;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;

import com.mongodb.DBCursor;

public class MongoFileCursor implements Iterator<MongoFile>, Iterable<MongoFile>, Closeable {

    private DBCursor cursor;
    private MongoFileStore store;

    /* package */MongoFileCursor(MongoFileStore store, DBCursor cursor) {

        this.store = store;
        this.cursor = cursor;

    }

    @Override
    public Iterator<MongoFile> iterator() {

        return new MongoFileCursor(store, cursor.copy()); // just what the DBCursor class does
    }

    @Override
    public boolean hasNext() {

        return this.cursor.hasNext();
    }

    @Override
    public MongoFile next() {

        return new MongoFile(store, this.cursor.next());
    }

    /**
     * Cannot remove from within a cursor
     */
    @Override
    public void remove()
            throws UnsupportedOperationException {

        throw new UnsupportedOperationException("can't remove from a cursor");
    }

    @Override
    public void close()
            throws IOException {

        this.cursor.close();
    }

}
