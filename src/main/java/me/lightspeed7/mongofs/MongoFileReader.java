package me.lightspeed7.mongofs;

import java.io.IOException;
import java.io.InputStream;

/**
 * Class for encapsulate the reader of data from a MongoFile
 * 
 * @author David Buschman
 * 
 */
public class MongoFileReader {

    private final MongoFile file;
    private MongoFileStore store;

    public MongoFileReader(MongoFileStore store, MongoFile mongoFile) {

        if (store == null) {
            throw new IllegalArgumentException("store cannot be null");
        }
        if (mongoFile == null) {
            throw new IllegalArgumentException("mongoFile cannot be null");
        }

        this.store = store;
        this.file = mongoFile;

    }

    /**
     * Create an input stream reader to pull the data from the
     * 
     * @return an InputStream ready for reading
     * @throws IOException
     */
    public final InputStream getInputStream() throws IOException {

        return file.getInputStream();
    }

    /**
     * Return the current mongoFile object for this reader
     * 
     * @return MongoFile
     */
    public MongoFile getMongoFile() {

        return file;
    }

    @Override
    public String toString() {

        return String.format("MongoFileReader [ file=%s, \n  store=%s\n]", file, store);
    }

}
