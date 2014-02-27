package me.lightspeed7.mongofs;

import java.io.Closeable;
import java.util.Iterator;
import java.util.List;

import com.mongodb.DBCursor;
import com.mongodb.DBDecoderFactory;
import com.mongodb.DBObject;
import com.mongodb.ReadPreference;
import com.mongodb.ServerAddress;

/**
 * This class is a surrogate holder for the actual DBCursor object underneath. I did not want to have to write this class, but I
 * felt the not have to type cast and given compile time explicit typing is too important in Java to force the users to have to
 * cast every object that come out of the cursor.
 * 
 * This object will be a pain to keep up to date with the DBCursor class in the mongo-java-driver and will most likely end up
 * tying versions of this library to specific versions on the mongo-java-driver in the future. Compile-time class type checking is
 * that important. ( IMHO )
 * 
 * @author David Buschman
 * 
 */
public class MongoFileCursor implements Iterator<MongoFile>, Iterable<MongoFile>, Closeable //
{

    private final DBCursor cursor;
    private final MongoFileStore store;

    /* package */MongoFileCursor(MongoFileStore store, DBCursor cursor) {

        this.store = store;
        this.cursor = cursor;

    }

    //
    // Cursor builder factory methods
    // /////////////////////////////////
    public MongoFileCursor copy() {

        return new MongoFileCursor(store, cursor.copy());
    }

    public MongoFileCursor sort(DBObject orderBy) {

        return new MongoFileCursor(store, cursor.sort(orderBy));
    }

    public MongoFileCursor addSpecial(String name, Object o) {

        return new MongoFileCursor(store, cursor.addSpecial(name, o));
    }

    public MongoFileCursor hint(DBObject indexKeys) {

        return new MongoFileCursor(store, cursor.hint(indexKeys));
    }

    public MongoFileCursor hint(String indexName) {

        return new MongoFileCursor(store, cursor.hint(indexName));
    }

    public MongoFileCursor snapshot() {

        return new MongoFileCursor(store, cursor.snapshot());
    }

    public MongoFileCursor limit(int n) {

        return new MongoFileCursor(store, cursor.limit(n));
    }

    public MongoFileCursor batchSize(int n) {

        return new MongoFileCursor(store, cursor.batchSize(n));
    }

    public MongoFileCursor skip(int n) {

        return new MongoFileCursor(store, cursor.skip(n));
    }

    // got ahead and enforce the deprecation right now
    // @Deprecated
    // public MongoFileCursor slaveOk() {
    //
    // return cursor.slaveOk();
    // }

    public MongoFileCursor addOption(int option) {

        return new MongoFileCursor(store, cursor.addOption(option));
    }

    public MongoFileCursor setOptions(int options) {

        return new MongoFileCursor(store, cursor.setOptions(options));
    }

    public MongoFileCursor resetOptions() {

        return new MongoFileCursor(store, cursor.resetOptions());
    }

    public MongoFileCursor setReadPreference(ReadPreference preference) {

        return new MongoFileCursor(store, cursor.setReadPreference(preference));
    }

    public ReadPreference getReadPreference() {

        return cursor.getReadPreference();
    }

    public MongoFileCursor setDecoderFactory(DBDecoderFactory fact) {

        return new MongoFileCursor(store, cursor.setDecoderFactory(fact));
    }

    //
    // MongoFile data methods
    // ////////////////////////////
    public Iterator<MongoFile> iterator() {

        return new MongoFileCursor(store, cursor.copy()); // its what the DBCursor class does
    }

    public MongoFile next() {

        return new MongoFile(store, cursor.next());
    }

    public MongoFile curr() {

        return new MongoFile(store, cursor.curr());
    }

    // /////////////////////////////////////////////////
    // I simply do not want to support the toArray methods
    //
    // public List<MongoFile> toArray() {
    //
    // return cursor.toArray();
    // }
    //
    // public List<MongoFile> toArray(int max) {
    //
    // return cursor.toArray(max);
    // }

    //
    // state accessors
    // /////////////////////

    public DBObject explain() {

        return cursor.explain();
    }

    public int getOptions() {

        return cursor.getOptions();
    }

    public int numGetMores() {

        return cursor.numGetMores();
    }

    public List<Integer> getSizes() {

        return cursor.getSizes();
    }

    public int numSeen() {

        return cursor.numSeen();
    }

    public boolean hasNext() {

        return cursor.hasNext();
    }

    public long getCursorId() {

        return cursor.getCursorId();
    }

    public void close() {

        cursor.close();
    }

    public void remove() {

        cursor.remove();
    }

    public int length() {

        return cursor.length();
    }

    public int itcount() {

        return cursor.itcount();
    }

    public int count() {

        return cursor.count();
    }

    public int size() {

        return cursor.size();
    }

    public DBObject getKeysWanted() {

        return cursor.getKeysWanted();
    }

    public DBObject getQuery() {

        return cursor.getQuery();
    }

    // hide this in this context, users do not need to see this here, get it from the store
    // public DBCollection getCollection() {
    //
    // return cursor.getCollection();
    // }

    public ServerAddress getServerAddress() {

        return cursor.getServerAddress();
    }

    public DBDecoderFactory getDecoderFactory() {

        return cursor.getDecoderFactory();
    }

    //
    // toString
    // /////////////////////////
    @Override
    public String toString() {

        return String.format("MongoFileCursor [ store=%s, \n  cursor=%s\n]", store, cursor);
    }

}
