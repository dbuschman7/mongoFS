package me.lightspeed7.mongofs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import me.lightspeed7.mongofs.common.MongoFileConstants;
import me.lightspeed7.mongofs.util.BytesCopier;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoException;

public class MongoFileStore {

    private static final Logger LOGGER = Logger.getLogger("me.davidbuschman.mongofs");

    public static final ChunkSize DEFAULT_CHUNKSIZE = ChunkSize.medium_256K;

    private final DBCollection filesCollection;
    private final DBCollection chunksCollection;

    private MongoFileStoreConfig config;

    /**
     * CTOR
     * 
     * @param database
     *            the MongoDB database to look for the collections in
     * @param config
     *            the configuration for this file store
     */
    public MongoFileStore(DB database, MongoFileStoreConfig config) {

        this.config = config;
        filesCollection = database.getCollection(config.getBucket() + ".files");
        filesCollection.setWriteConcern(config.getWriteConcern());
        filesCollection.setReadPreference(config.getReadPreference());
        filesCollection.setObjectClass(BasicDBObject.class);

        chunksCollection = database.getCollection(config.getBucket() + ".chunks");
        chunksCollection.setWriteConcern(config.getWriteConcern());
        chunksCollection.setReadPreference(config.getReadPreference());
        checkForPOwerOf2Sizes(chunksCollection);

        // ensure standard indexes as long as collections are small
        try {
            if (filesCollection.count() < 1000) {
                filesCollection.ensureIndex(BasicDBObjectBuilder.start().add("filename", 1).add("uploadDate", 1).get());
            }
            if (chunksCollection.count() < 1000) {
                chunksCollection.ensureIndex(BasicDBObjectBuilder.start().add("files_id", 1).add("n", 1).get(),
                        BasicDBObjectBuilder.start().add("unique", true).get());

            }
        } catch (MongoException e) {
            LOGGER.info(String.format("Unable to ensure indices on GridFS collections in database %s", //
                    filesCollection.getDB().getName()));
        }
    }

    private void checkForPOwerOf2Sizes(DBCollection coll) {

        boolean isSet = false;

        if (!isSet) {
            DBObject command = BasicDBObjectBuilder//
                    .start("collMod", coll.getName()).append("usePowerOf2Sizes", Boolean.TRUE.booleanValue()).get();
            coll.getDB().command(command);
        }

    }

    //
    // public
    // ///////////////

    public int getChunkSize() {

        return config.getChunkSize();
    }

    /**
     * Create a new file entry in the datastore, then a MongoFile object to start writing to it.
     * 
     * NOTE : the system will determine if compression is needed
     * 
     * @param filename
     *            the name of the new file
     * @param mediaType
     *            the media type of the data
     * 
     * @return a writer to write datq to for this file
     * 
     * @throws IOException
     *             if an error occurs during reading and/or writing
     * @throws IllegalArgumentException
     *             if required parameters are null
     */
    public MongoFileWriter createNew(String filename, String mediaType)
            throws IOException, IllegalArgumentException {

        return createNew(filename, mediaType, true);
    }

    /**
     * Create a new file entry in the datastore, then a MongoFile object to start writing to it.
     * 
     * NOTE : if compress = false and the media type is compressible, the file will not be stored compressed in the store
     * 
     * @param filename
     *            the name of the new file
     * @param mediaType
     *            the media type of the data
     * 
     * @param compress
     *            should use compression if the mime type allows ( zip files will not be compressed even compress = true )
     * 
     * @return a writer to write datq to for this file
     * 
     * @throws IOException
     *             if an error occurs during reading and/or writing
     * @throws IllegalArgumentException
     *             if required parameters are null
     */
    public MongoFileWriter createNew(String filename, String mediaType, boolean compress)
            throws IOException, IllegalArgumentException {

        if (filename == null) {
            throw new IllegalArgumentException("filename cannot be null");
        }
        if (mediaType == null) {
            throw new IllegalArgumentException("mediaType cannot be null");
        }

        // send wrapper object
        MongoFileUrl mongoFileUrl = MongoFileUrl//
                .construct(new ObjectId().toString(), filename, mediaType, null, compress);
        return new MongoFileWriter(mongoFileUrl, //
                new MongoFile(this, mongoFileUrl, config.getChunkSize(), compress), chunksCollection);
    }

    /**
     * Upload a file to the datastore from the filesystem
     * 
     * @param file
     *            - the file object to get the data from
     * @param mediaType
     *            the media type of the data
     * 
     * @return the MongoFile object created for this file object
     * 
     * @throws IOException
     *             if an error occurs during reading and/or writing
     * @throws IllegalArgumentException
     *             if required parameters are null
     * @throws FileNotFoundException
     *             if the file does not exist or cannot be read
     */
    public MongoFile upload(File file, String mediaType)
            throws IOException, IllegalArgumentException {

        return upload(file.toPath().toString(), mediaType, true, new FileInputStream(file));
    }

    /**
     * Upload a file to the datastore from the filesystem
     * 
     * @param file
     *            - the file object to get the data from
     * @param mediaType
     *            the media type of the data
     * @param compress
     *            allow compression to be used if applicable
     * 
     * @return the MongoFile object created for this file object
     * 
     * @throws IOException
     *             if an error occurs during reading and/or writing
     * @throws IllegalArgumentException
     *             if required parameters are null
     * @throws FileNotFoundException
     *             if the file does not exist or cannot be read
     */
    public MongoFile upload(File file, String mediaType, boolean compress)
            throws IOException, IllegalArgumentException {

        if (file == null) {
            throw new IllegalArgumentException("passed in file cannot be null");
        }

        if (!file.exists()) {
            throw new FileNotFoundException("File does not exist or cannot be read by this library");
        }

        return upload(file.toPath().toString(), mediaType, compress, new FileInputStream(file));
    }

    /**
     * Upload a file to the datastore from any InputStream
     * 
     * @param filename
     *            the name of the file to use
     * @param mediaType
     *            the media type of the data
     * @param inputStream
     *            the stream object to read the data from
     * 
     * @return the MongoFile object created for this file object
     * 
     * @throws IOException
     *             if an error occurs during reading and/or writing
     * @throws IllegalArgumentException
     *             if required parameters are null
     */
    public MongoFile upload(String filename, String mediaType, InputStream inputStream)
            throws IOException, IllegalArgumentException {

        return upload(filename, mediaType, true, inputStream);

    }

    /**
     * Upload a file to the datastore from any InputStream
     * 
     * @param filename
     *            the name of the file to use
     * @param mediaType
     *            the media type of the data
     * @param inputStream
     *            the stream object to read the data from
     * @param compress
     *            allow compression to be used if applicable
     * 
     * @return the MongoFile object created for this file object
     * 
     * @throws IOException
     *             if an error occurs during reading and/or writing
     * @throws IllegalArgumentException
     *             if required parameters are null
     */
    public MongoFile upload(String filename, String mediaType, boolean compress, InputStream inputStream)
            throws IOException, IllegalArgumentException {

        return createNew(filename, mediaType).write(inputStream);
    }

    /**
     * Returns a reader for the passed in URL
     * 
     * @param url
     * 
     * @return a reader object
     * 
     * @throws MongoException
     * @throws IllegalArgumentException
     *             if required parameters are null
     */
    public MongoFile getFile(URL url)
            throws MongoException, IllegalArgumentException {

        if (url == null) {
            throw new IllegalArgumentException("url cannot be null");
        }
        return getFile(MongoFileUrl.construct(url));
    }

    /**
     * Returns a reader for the passed in file object
     * 
     * @param url
     * 
     * @return a reader object
     * 
     * @throws MongoException
     * @throws IllegalArgumentException
     *             if required parameters are null
     */
    public MongoFile getFile(MongoFileUrl url)
            throws MongoException, IllegalArgumentException {

        if (url == null) {
            throw new IllegalArgumentException("url cannot be null");
        }
        BasicDBObject file = (BasicDBObject) filesCollection.findOne(//
                BasicDBObjectBuilder.start(MongoFileConstants._id.toString(), url.getMongoFileId()).get());

        return file == null ? null : new MongoFile(this, file);
    }

    /**
     * Returns true is the file exists in the data store
     * 
     * @param url
     * 
     * @return true if the file exists in the datastore
     * 
     * @throws MongoException
     */
    public boolean exists(MongoFileUrl url)
            throws MongoException {

        if (url == null) {
            throw new IllegalArgumentException("mongoFile cannot be null");
        }
        BasicDBObject file = (BasicDBObject) filesCollection.findOne(//
                BasicDBObjectBuilder.start(MongoFileConstants._id.toString(), url.getMongoFileId()).get());
        return file != null;
    }

    /**
     * Run a test command to the mongoDB to test connectivity and the server is running
     * 
     * @return true if a connection could be made
     * 
     * @throws MongoException
     */
    public boolean validateConnection()
            throws MongoException {

        try {
            String command = String
                    .format("{ touch: \"%s\", data: false, index: true }", config.getBucket() + ".files");
            filesCollection.getDB().command(command).throwOnError();
            return true;
        } catch (Exception e) {
            throw new MongoException("Unable to run command on server", e);
        }
    }

    /**
     * Return an input stream to read the file content data from
     * 
     * @param file
     *            the MongoFile object
     * 
     * @return an input stream to read from
     * 
     * @throws IOException
     */
    public InputStream read(MongoFile file)
            throws IOException {

        return new MongoFileReader(this, file).getInputStream();
    }

    /**
     * Return a dynamic query object to do ad-hoc file lookups
     * 
     * @return a query object
     */
    public MongoFileQuery query() {

        return new MongoFileQuery(this);
    }

    /**
     * Copy the content to the given output stream
     * 
     * @param file
     *            the MongoFile to lookup
     * 
     * @param out
     *            the output stream to write to
     * 
     * @param flush
     *            should the output stream be flush when all the data has been written.
     * 
     * @throws IOException
     */
    public void read(MongoFile file, OutputStream out, boolean flush)
            throws IOException {

        new BytesCopier(new MongoFileReader(this, file).getInputStream(), out).transfer(flush);
    }

    //
    // remove methods
    // ////////////////////

    /**
     * Remove a file from the database identified by the given MongoFile
     * 
     * @param mongoFile
     * @throws IllegalArgumentException
     * @throws MongoException
     * @throws IOException
     */
    public void remove(MongoFile mongoFile)
            throws IllegalArgumentException, MongoException, IOException {

        if (mongoFile == null) {
            throw new IllegalArgumentException("mongoFile cannot be null");
        }
        remove(mongoFile.getURL());
    }

    /**
     * Remove a file from the datastore identified by the given MongoFileUrl
     * 
     * @param url
     *            - the MongoFileUrl
     * 
     * @throws IOException
     *             if an error occurs during reading and/or writing
     * @throws IllegalArgumentException
     *             if required parameters are null
     */
    public void remove(MongoFileUrl url)
            throws IllegalArgumentException, MongoException {

        if (url == null) {
            throw new IllegalArgumentException("mongoFileUrl cannot be null");
        }

        filesCollection.remove(new BasicDBObject("_id", url.getMongoFileId()));
        chunksCollection.remove(new BasicDBObject("files_id", url.getMongoFileId()));
    }

    /**
     * Delete all files that match the given criteria
     * 
     * This code was taken from -- https://github.com/mongodb/mongo-java-driver/pull/171
     * 
     * @param query
     *            the selection criteria
     */
    public void remove(DBObject query) {

        if (query == null) {
            throw new IllegalArgumentException("query can not be null");
        }
        // can't remove chunks without files_id thus keep them
        List<ObjectId> filesIds = new ArrayList<ObjectId>();
        for (MongoFile f : query().find(query)) {
            filesIds.add((ObjectId) f.getId());
        }

        // remove files from bucket
        getFilesCollection().remove(query);
        // then remove chunks
        getChunksCollection().remove(new BasicDBObject("files_id", new BasicDBObject("$in", filesIds)));
    }

    //
    // collection getters
    // /////////////////////////

    /**
     * The underlying MongoDB collection object for files
     * 
     * @return the DBCollection object
     */
    public DBCollection getFilesCollection() {

        return filesCollection;
    }

    /**
     * The underlying MongoDB collection object
     * 
     * @return the DBCollection object
     */
    public DBCollection getChunksCollection() {

        return chunksCollection;
    }

    @Override
    public String toString() {

        return String.format("MongoFileStore [filesCollection=%s, chunksCollection=%s,\n  config=%s\n]",
                filesCollection, chunksCollection, config.toString());
    }

}
