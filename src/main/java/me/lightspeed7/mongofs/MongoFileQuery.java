package me.lightspeed7.mongofs;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;

/**
 * This call holds all of the generic query methods and lookups matheds for metatdata type lookups
 * 
 * @author David Buschman
 * 
 */
public class MongoFileQuery {

    private MongoFileStore store;

    /* package */MongoFileQuery(MongoFileStore store) {

        this.store = store;
    }

    // --------------------------
    // ------ reading -------
    // --------------------------

    /**
     * finds one file matching the given id. Equivalent to findOne(id)
     * 
     * @param id
     * @return
     * @throws MongoException
     */
    public MongoFile find(ObjectId id) {

        return findOne(id);
    }

    /**
     * finds one file matching the given id.
     * 
     * @param id
     * @return
     * @throws MongoException
     */
    public MongoFile findOne(ObjectId id) {

        return findOne(new BasicDBObject("_id", id));
    }

    /**
     * finds one file matching the given filename
     * 
     * @param filename
     * @return
     * @throws MongoException
     */
    public MongoFile findOne(String filename) {

        return findOne(new BasicDBObject("filename", filename));
    }

    /**
     * finds one file matching the given query
     * 
     * @param query
     * @return
     * @throws MongoException
     */
    public MongoFile findOne(DBObject query) {

        return _fix(store.getFilesCollection().findOne(query));
    }

    /**
     * finds a list of files matching the given filename
     * 
     * @param filename
     * @return
     * @throws MongoException
     */
    public MongoFileCursor find(String filename) {

        return find(filename, null);
    }

    /**
     * finds a list of files matching the given filename
     * 
     * @param filename
     * @param sort
     * @return
     * @throws MongoException
     */
    public MongoFileCursor find(String filename, DBObject sort) {

        return find(new BasicDBObject("filename", filename), sort);
    }

    /**
     * finds a list of files matching the given query
     * 
     * @param query
     * @return
     * @throws MongoException
     */
    public MongoFileCursor find(DBObject query) {

        return find(query, null);
    }

    /**
     * finds a list of files matching the given query
     * 
     * @param query
     * @param sort
     * @return
     * @throws MongoException
     */
    public MongoFileCursor find(DBObject query, DBObject sort) {

        DBCursor c = store.getFilesCollection().find(query);
        if (sort != null) {
            c.sort(sort);
        }
        return new MongoFileCursor(store, c);
    }

    /**
     * Gets the list of files stored in this mongoFS, sorted by filename.
     * 
     * @return cursor of file objects
     */
    public MongoFileCursor getFileList() {

        return getFileList(null);
    }

    /**
     * Gets a filtered list of files stored in this mongoFS, sorted by filename.
     * 
     * @param query
     *            filter to apply
     * @return cursor of file objects
     */
    public MongoFileCursor getFileList(final DBObject query) {

        DBCollection coll = store.getFilesCollection();

        DBCursor cursor = new DBCursor(coll, query, null, coll.getReadPreference());

        return new MongoFileCursor(store, cursor);
    }

    /**
     * Gets a sorted, filtered list of files stored in this mongoFS.
     * 
     * @param query
     *            filter to apply
     * @param sort
     *            sorting to apply
     * @return cursor of file objects
     */
    public MongoFileCursor getFileList(final DBObject query, final DBObject sort) {

        DBCollection coll = store.getFilesCollection();

        @SuppressWarnings( "resource" )
        DBCursor cursor = new DBCursor(coll, query, null, coll.getReadPreference());

        return new MongoFileCursor(store, cursor.sort(sort));
    }

    //
    // internal
    // //////////////

    protected MongoFile _fix(Object o) {

        if (o == null)
            return null;

        if (!(o instanceof DBObject))
            throw new RuntimeException("somehow didn't get a DBObject");

        return new MongoFile(store, (DBObject) o);
    }
}
