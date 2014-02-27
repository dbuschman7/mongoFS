package me.lightspeed7.mongofs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Logger;

import me.lightspeed7.mongofs.common.MongoFileConstants;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoException;
import com.mongodb.WriteConcern;

public class MongoFileStore {

    private static final Logger LOGGER = Logger.getLogger("me.davidbuschman.mongofs");

    public static final int DEFAULT_CHUNKSIZE = 256 * 1024;

    final DBCollection filesCollection;
    final DBCollection chunksCollection;

    private int chunkSize = DEFAULT_CHUNKSIZE;

    private String bucket;

    /**
     * CTOR
     * 
     * NOTE: be sure to set the correct write concern on this database object to MonogFS to use.
     * 
     * NOTE: Uses a write concern of NORMAL
     * 
     * @param database
     *            the MongoDB database to look for the collections in.
     * @param bucket
     *            - the mane of the bucket for these files.
     */
    public MongoFileStore(DB database, String bucket) {

        this(database, bucket, WriteConcern.NORMAL);
    }

    /**
     * CTOR
     * 
     * @param database
     *            the MongoDB database to look for the collections in
     * @param bucket
     *            the mane of the bucket for these files
     * @param writeConcern
     *            the writeConcern that should be used for both collections
     */
    public MongoFileStore(DB database, String bucket, WriteConcern writeConcern) {

        this.bucket = bucket;
        filesCollection = database.getCollection(bucket + ".files");
        filesCollection.setWriteConcern(writeConcern);
        filesCollection.setObjectClass(BasicDBObject.class);

        chunksCollection = database.getCollection(bucket + ".chunks");
        chunksCollection.setWriteConcern(writeConcern);

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

    //
    // public
    // ///////////////

    public int getChunkSize() {

        return chunkSize;
    }

    public void setChunkSize(int chunkSize) {

        this.chunkSize = chunkSize;
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
                new MongoFile(this, mongoFileUrl, this.chunkSize, compress), chunksCollection);
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
    public MongoFile upload(String filename, String mediaType, InputStream is)
            throws IOException, IllegalArgumentException {

        return upload(filename, mediaType, true, is);

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
    public MongoFile upload(String filename, String mediaType, boolean compress, InputStream is)
            throws IOException, IllegalArgumentException {

        return createNew(filename, mediaType).write(is);
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
            String command = String.format("{ touch: \"%s\", data: false, index: true }", this.bucket + ".files");
            filesCollection.getDB().command(command).throwOnError();
            return true;
        } catch (Exception e) {
            throw new MongoException("Unable to run command on server", e);
        }
    }

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
     * @param MongoFileUrl
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

        return String.format("MongoFileStore [filesCollection=%s, chunksCollection=%s, chunkSize=%s]", filesCollection,
                chunksCollection, chunkSize);
    }

}
