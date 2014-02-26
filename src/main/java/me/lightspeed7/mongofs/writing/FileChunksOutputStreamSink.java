package me.lightspeed7.mongofs.writing;

import java.io.IOException;
import java.io.OutputStream;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;

/**
 * A sink object the absorbs all the chunks that are sent to it and create MongoDB chunks for each one.
 * 
 * Place this object behind the BufferedChunksOutputStream
 * 
 * @author David Buschman
 * 
 */
public class FileChunksOutputStreamSink extends OutputStream {

    private Object id;
    private DBCollection collection;
    private int currentChunkNumber = 0;
    private ChunksStatisticsAdapter adapter;

    public FileChunksOutputStreamSink(DBCollection collection, Object fileId, ChunksStatisticsAdapter adapter) {

        this.collection = collection;
        this.id = fileId;
        this.adapter = adapter;
    }

    @Override
    public void write(int b)
            throws IOException {

        throw new IllegalStateException("Single byte writing not supported with this OutputStream");
    }

    @Override
    public void write(byte[] b)
            throws IOException {

        if (b == null) {
            throw new IllegalArgumentException("buffer cannot be null");
        }

        super.write(b, 0, b.length);
    }

    @Override
    public void write(byte[] buffer, int offset, int length)
            throws IOException {

        byte[] internal = buffer; // assume the whole passed in buffer for efficiency

        // if partial buffer, then we have to copy the data until serialized
        if (offset != 0 || length != buffer.length) {
            internal = new byte[length];
            System.arraycopy(buffer, offset, internal, 0, length);
        }

        // construct the chunk
        BasicDBObject dbObject = new BasicDBObject("files_id", id)//
                .append("n", currentChunkNumber)// Sequence number of the chunk in the file
                .append("sz", length)// length of the chunk data portion on the chunk
                .append("data", internal)// the data encoded
        ;
        ++currentChunkNumber;

        // persist it
        collection.save(dbObject);
        adapter.collectFromChunk(dbObject);
    }

    @Override
    public void flush()
            throws IOException {

        adapter.flush();
    }

    @Override
    public void close()
            throws IOException {

        adapter.close();
    }

}
