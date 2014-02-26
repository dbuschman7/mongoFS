package me.lightspeed7.mongofs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import me.lightspeed7.mongofs.common.MongoFileConstants;
import me.lightspeed7.mongofs.util.BytesCopier;
import me.lightspeed7.mongofs.writing.BufferedChunksOutputStream;
import me.lightspeed7.mongofs.writing.CountingOutputStream;
import me.lightspeed7.mongofs.writing.FileChunksOutputStreamSink;
import me.lightspeed7.mongofs.writing.MongoGZipOutputStream;

import com.mongodb.DBCollection;

public class MongoFileWriter {

    private MongoFile file;
    private MongoFileUrl url;
    private DBCollection chunksCollection;

    public MongoFileWriter(MongoFileUrl url, MongoFile file, DBCollection chunksCollection) {

        this.url = url;
        this.file = file;
        this.chunksCollection = chunksCollection;
    }

    /**
     * Stream the data to the file
     * 
     * @param in
     * @return the fileurl object
     * 
     * @throws IOException
     * @throws IllegalArgumentException
     */
    public MongoFile write(InputStream in)
            throws IOException, IllegalArgumentException {

        if (in == null) {
            throw new IllegalArgumentException("passed inputStream cannot be null");
        }

        // set up the chunk writing
        MongoFileWriterAdapter adapter = new MongoFileWriterAdapter(file);
        FileChunksOutputStreamSink chunks = new FileChunksOutputStreamSink(chunksCollection, file.getId(), adapter);
        OutputStream sink = new BufferedChunksOutputStream(chunks, file.getChunkSize());

        // transfer the data
        try (OutputStream out = determineTopOutputStream(sink)) {
            new BytesCopier(in, out).transfer(true);
        }

        // make sure all the bytes transferred correctly
        file.validate();

        // return the file object
        return file;

    }

    private OutputStream determineTopOutputStream(OutputStream sink)
            throws IOException {

        if (url.isStoredCompressed()) {
            return new MongoGZipOutputStream(file, sink);
        } else {
            return new CountingOutputStream(MongoFileConstants.length, file, sink);
        }
    }

    /**
     * The the MongoFile object to write to
     * 
     * @return
     */
    public MongoFile getMongoFile() {

        return file;
    }

}
