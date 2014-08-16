package me.lightspeed7.mongofs;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import me.lightspeed7.mongofs.reading.CountingInputStream;
import me.lightspeed7.mongofs.reading.FileChunksInputStreamSource;

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

        // returned <- counting <- chunks
        //
        // or
        //
        // returned <- gzip <- counting <- chunks
        InputStream returned = new FileChunksInputStreamSource(store, file);
        returned = new CountingInputStream(returned);

        if (file.getURL().isStoredCompressed()) {
            returned = new GZIPInputStream(returned);
        }

        return returned;
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
