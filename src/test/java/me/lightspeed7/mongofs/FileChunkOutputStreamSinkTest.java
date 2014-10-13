package me.lightspeed7.mongofs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import me.lightspeed7.mongofs.ChunksStatisticsAdapter;
import me.lightspeed7.mongofs.FileChunksOutputStreamSink;
import me.lightspeed7.mongofs.gridfs.GridFS;
import me.lightspeed7.mongofs.gridfs.GridFSInputFile;
import me.lightspeed7.mongofs.gridfs.GridFSInputFileAdapter;

import org.junit.Before;
import org.junit.Test;
import org.mongodb.Document;
import org.mongodb.MongoCollection;
import org.mongodb.MongoCursor;
import org.mongodb.MongoView;

import com.mongodb.DB;
import com.mongodb.MongoClient;

public class FileChunkOutputStreamSinkTest {

    private static final String DB_NAME = "MongoFS-FileChunkOutputStreamSink";

    private DB database;
    // private ObjectId id;

    private MongoClient mongoClient;

    private GridFS gridFS;

    private String bucketName;

    // private DBCollection filesCollection;

    private MongoCollection<Document> chunksCollection;

    @Before
    public void before() {

        mongoClient = MongoTestConfig.construct();

        database = mongoClient.getDB(DB_NAME);
        gridFS = new GridFS(database);
        bucketName = "buffer";
        // filesCollection = database.getCollection(bucketName + ".files");
        chunksCollection = new MongoCollection<Document>(database.getCollection(bucketName + ".chunks"));

        // id = new ObjectId();
    }

    @Test
    public void testFullBufferWrite() throws IOException {

        GridFSInputFile file = gridFS.createFile("foo");
        ChunksStatisticsAdapter adapter = new GridFSInputFileAdapter(file);

        FileChunksOutputStreamSink sink = new FileChunksOutputStreamSink(//
                chunksCollection, file.getId(), adapter, null);
        try {
            byte[] array = "This is a test".getBytes();
            sink.write(array, 0, array.length);
        } finally {
            sink.close();
        }

        // assert
        MongoView<Document> view = chunksCollection.find(new Document("files_id", file.getId()));
        MongoCursor<Document> cursor = view.get();

        assertTrue(cursor.hasNext());

        Document found = cursor.next();

        assertNotNull(found.get("files_id"));
        assertEquals(file.getId(), found.get("files_id"));

        assertNotNull(found.get("data"));
        byte[] bytes = (byte[]) found.get("data");
        assertEquals(14, bytes.length);
        assertEquals("This is a test", new String(bytes, "UTF-8"));

    }

    @Test
    public void testPartialBufferWrite() throws IOException {

        GridFSInputFile file = gridFS.createFile("bar");
        GridFSInputFileAdapter adapter = new GridFSInputFileAdapter(file);

        FileChunksOutputStreamSink sink = new FileChunksOutputStreamSink(//
                chunksCollection, file.getId(), adapter, null);
        try {
            byte[] array = "This is a test".getBytes();
            sink.write(array, 10, 4);
        } finally {
            sink.close();
        }

        // assert
        MongoView<Document> view = chunksCollection.find(new Document("files_id", file.getId()));
        MongoCursor<Document> cursor = view.get();

        assertTrue(cursor.hasNext());

        Document found = cursor.next();

        assertNotNull(found.get("files_id"));
        assertEquals(file.getId(), found.get("files_id"));

        assertNotNull(found.get("data"));
        byte[] bytes = (byte[]) found.get("data");
        assertEquals(4, bytes.length);

        assertEquals("test", new String(bytes, "UTF-8"));
    }
}
