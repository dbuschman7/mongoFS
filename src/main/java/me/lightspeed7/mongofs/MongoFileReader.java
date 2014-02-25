package me.lightspeed7.mongofs;

import java.io.InputStream;

import com.mongodb.gridfs.GridFSDBFile;

/**
 * Class for encapsulate the reader of data from a MongoFile
 * 
 * @author David Buschman
 * 
 */
public class MongoFileReader {

    private final MongoFileUrl mongoFile;
    private final GridFSDBFile file;

    /* package */MongoFileReader(MongoFileUrl mongoFile, GridFSDBFile file) {

        this.mongoFile = mongoFile;
        this.file = file;
    }

    /**
     * Create an input stream reader to pull the data from the
     * 
     * @return an InputStream ready for reading
     */
    public final InputStream getInputStream() {

        return this.file.getInputStream();
    }

    /**
     * Return the current mongoFile object for this reader
     * 
     * @return MongoFile
     */
    public MongoFileUrl getMongoFile() {

        return mongoFile;
    }

}
