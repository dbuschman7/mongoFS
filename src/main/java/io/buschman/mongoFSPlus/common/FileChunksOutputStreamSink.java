package io.buschman.mongoFSPlus.common;

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
    public void write(byte[] b, int off, int len)
            throws IOException {

        byte[] internal = b; // assume whole buffer

        // if partial buffer, then we have to copy the data until serialized
        if (off != 0 || len != b.length) {
            internal = new byte[len];
            System.arraycopy(b, off, internal, 0, len);
        }

        BasicDBObject dbObject = new BasicDBObject("files_id", id)//
                .append("n", currentChunkNumber)//
                .append("data", internal);
        ++currentChunkNumber;

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
