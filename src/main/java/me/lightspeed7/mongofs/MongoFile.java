package me.lightspeed7.mongofs;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import me.lightspeed7.mongofs.common.InputFile;
import me.lightspeed7.mongofs.common.MongoFileConstants;
import me.lightspeed7.mongofs.util.DBObjectWrapper;
import sun.net.www.protocol.mongofile.Parser;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.util.JSON;

public class MongoFile implements InputFile {

    private static final String METADATA = "metadata";

    DBObject surrogate;

    private MongoFileStore store;

    private boolean compress = true;

    /**
     * Construct a MongoFile object for reading data
     * 
     * @param store
     * @param o
     */
    /* package */MongoFile(MongoFileStore store, DBObject o) {

        this.store = store;
        this.surrogate = o;
    }

    /**
     * Construct a MongoFile object for writing data
     * 
     * @param collection
     * @param url
     */
    /* package */MongoFile(MongoFileStore store, MongoFileUrl url, int chunkSize, boolean compress) {

        this.store = store;
        this.compress = compress;
        this.surrogate = new BasicDBObject(10);

        this.surrogate.put(MongoFileConstants._id.toString(), url.getMongoFileId());

        this.surrogate.put(MongoFileConstants.chunkSize.toString(), chunkSize);
        this.surrogate.put(MongoFileConstants.filename.toString(), url.getFilePath());
        this.surrogate.put(MongoFileConstants.contentType.toString(), url.getMediaType());
        this.surrogate.put(MongoFileConstants.compressionFormat.toString(), url.getCompresionFormat());
    }

    private String getBucketName() {

        return this.store.filesCollection.getName().split("\\.")[0];
    }

    /**
     * Saves the file entry to the files collection
     * 
     * @throws MongoException
     */
    public void save() {

        this.store.filesCollection.save(surrogate);
    }

    /**
     * Verifies that the MD5 matches between the database and the local file. This should be called after transferring a file.
     * 
     * @throws MongoException
     */
    public void validate() {

        String md5key = MongoFileConstants.md5.toString();
        String md5 = new DBObjectWrapper(surrogate).getString(md5key);
        if (md5 == null) {
            throw new MongoException("no md5 stored");
        }

        DBObject cmd = new BasicDBObject("filemd5", surrogate.get(MongoFileConstants._id.toString()));
        cmd.put("root", getBucketName());
        DBObject res = this.store.filesCollection.getDB().command(cmd);
        if (res != null && res.containsField(md5key)) {
            String m = res.get(md5key).toString();
            if (m.equals(md5)) {
                return;
            }
            throw new MongoException("md5 differ.  mine [" + md5 + "] theirs [" + m + "]");
        }

        // no md5 from the server
        throw new MongoException("no md5 returned from server: " + res);

    }

    public MongoFileUrl getURL()
            throws MalformedURLException {

        if (surrogate != null) {
            URL url = Parser.construct(this.getId().toString(), this.getFilename(), this.getContentType(),
                    (String) this.get(MongoFileConstants.compressionFormat.toString()), false);
            return MongoFileUrl.construct(url);
        }
        return MongoFileUrl.construct(this.getId().toString(), this.getFilename(), this.getContentType(),
                (String) this.get(MongoFileConstants.compressionFormat.toString()), compress);
    }

    /**
     * Returns the number of chunks that store the file data.
     * 
     * @return number of chunks
     */
    public int getChunkCount() {

        if (surrogate == null) {
            throw new IllegalArgumentException("Cannot get chunk count before data is written");
        }
        return new DBObjectWrapper(surrogate).getInt(MongoFileConstants.chunkCount.toString());
    }

    /**
     * Gets the id.
     * 
     * @return the id of the file.
     */
    public Object getId() {

        return surrogate.get("_id");
    }

    /**
     * Gets the filename.
     * 
     * @return the name of the file
     */
    public String getFilename() {

        return new DBObjectWrapper(surrogate).getString(MongoFileConstants.filename.toString());
    }

    /**
     * Gets the content type.
     * 
     * @return the content type
     */
    public String getContentType() {

        return new DBObjectWrapper(surrogate).getString(MongoFileConstants.contentType.toString());
    }

    /**
     * Gets the file's length.
     * 
     * @return the length of the file
     */
    public long getLength() {

        return new DBObjectWrapper(surrogate).getLong(MongoFileConstants.length.toString());
    }

    /**
     * Gets the size of a chunk.
     * 
     * @return the chunkSize
     */
    public int getChunkSize() {

        return new DBObjectWrapper(surrogate).getInt(MongoFileConstants.chunkSize.toString());
    }

    /**
     * Gets the upload date.
     * 
     * @return the date
     */
    public Date getUploadDate() {

        return (Date) surrogate.get(MongoFileConstants.uploadDate.toString());
    }

    /**
     * Gets the aliases from the metadata. note: to set aliases, call put( "aliases" , List<String> )
     * 
     * @return list of aliases
     */
    @SuppressWarnings( "unchecked" )
    public List<String> getAliases() {

        return (List<String>) surrogate.get(MongoFileConstants.aliases.toString());
    }

    /**
     * Gets the file metadata.
     * 
     * @return the metadata
     */
    public DBObject getMetaData() {

        return (DBObject) surrogate.get(METADATA);
    }

    /**
     * Gets the file metadata.
     * 
     * @param metadata
     *            metadata to be set
     */
    public void setMetaData(final DBObject metadata) {

        surrogate.put(METADATA, metadata);
    }

    /**
     * Add an object to the metadata subclass
     * 
     * @param key
     * @param value
     */
    public Object setInMetaData(String key, Object value) {

        DBObject object = (DBObject) surrogate.get(METADATA);
        if (object == null) {
            object = new BasicDBObject();
            surrogate.put(METADATA, object);
        }
        return object.put(key, value);
    }

    /**
     * Gets the observed MD5 during transfer
     * 
     * @return md5
     */
    public String getMD5() {

        return new DBObjectWrapper(surrogate).getString(MongoFileConstants.md5.toString());
    }

    /**
     * Put a value into the object for a given key
     * 
     * @param key
     * @param value
     * @return the previous value
     */
    public Object put(final String key, final Object v) {

        if (key == null) {
            throw new RuntimeException("key should never be null");
        }

        return surrogate.put(key, v);
    }

    /**
     * Get an the value on any attribute in the system
     * 
     * @param key
     * @return the current value
     */
    public Object get(final String key) {

        if (key == null) {
            throw new IllegalArgumentException("Key should never be null");
        }
        return surrogate.get(key);
    }

    /**
     * Return the value for the given key as a string
     * 
     * @param key
     * 
     * @return the string value
     */
    public String getString(String key) {

        return (String) surrogate.get(key);
    }

    /**
     * Return the value for the given key as a integer
     * 
     * @param key
     * 
     * @return
     */
    public int getInt(String key) {

        if (key == null) {
            throw new IllegalArgumentException("key cannot be null");
        }

        Object value = surrogate.get(key);
        if (value == null) {
            return -1;
        }

        return Integer.parseInt(value.toString());
    }

    /**
     * Return the value for the given key as a long
     * 
     * @param key
     * 
     * @return the value as a long
     */
    public long getLong(String key) {

        if (key == null) {
            throw new IllegalArgumentException("key cannot be null");
        }

        Object value = surrogate.get(key);
        if (value == null) {
            return -1;
        }

        return Long.parseLong(value.toString());
    }

    /**
     * Does a key exist in the object
     * 
     * @param key
     * 
     * @return true if it exists
     */
    public boolean containsField(final String key) {

        return keySet().contains(key);
    }

    public Set<String> keySet() {

        Set<String> keys = new HashSet<String>();
        keys.addAll(MongoFileConstants.getCoreFields(true));
        keys.addAll(surrogate.keySet());
        return keys;
    }

    @Override
    public String toString() {

        return JSON.serialize(surrogate);
    }

}
