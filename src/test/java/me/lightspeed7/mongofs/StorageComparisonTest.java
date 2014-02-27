package me.lightspeed7.mongofs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;

import me.lightspeed7.mongofs.common.MongoFileConstants;
import me.lightspeed7.mongofs.util.BytesCopier;
import me.lightspeed7.mongofs.util.JSONHelper;

import org.junit.BeforeClass;
import org.junit.Test;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;

public class StorageComparisonTest implements LoremIpsum {

    private static final String DB_NAME = "MongoFSTest-comparison";

    private static DB database;

    private static MongoClient mongoClient;

    // initializer
    @BeforeClass
    public static void initial() {

        mongoClient = MongoTestConfig.construct();

        mongoClient.dropDatabase(DB_NAME);
        database = mongoClient.getDB(DB_NAME);

    }

    @Test
    public void testOriginalGridFS()
            throws IOException {

        String bucket = "original";
        com.mongodb.gridfs.GridFS gridFS = new com.mongodb.gridfs.GridFS(database, bucket);
        com.mongodb.gridfs.GridFSInputFile file = gridFS.createFile("originalGridFS.txt");
        file.put(MongoFileConstants.chunkCount.toString(), 0);
        file.put("aliases", Arrays.asList("one", "two", "three"));
        file.setMetaData(new BasicDBObject("key", "value"));

        try (OutputStream stream = file.getOutputStream()) {
            byte[] bytes = LOREM_IPSUM.getBytes();
            stream.write(bytes);
        }

        System.out.println("Original GridFS (2.11.4)");
        System.out.println("==============================");
        System.out.println(String.format("id = %s, filepath = %s", file.getId(), file.getFilename()));
        System.out.println("==============================");
        System.out.println(JSONHelper.prettyPrint(file.toString()));
        System.out.println("======");
        dumpChunks(bucket, file.getId(), System.out);
        System.out.println("==============================");
        System.out.println();

        // md5 validation
        try {
            file.validate();
        } catch (MongoException e) {
            fail(e.getMessage());
        }

        assertEquals(com.mongodb.gridfs.GridFS.DEFAULT_CHUNKSIZE, file.getChunkSize());
        assertEquals(1, file.numChunks());

        com.mongodb.gridfs.GridFSDBFile findOne = gridFS.findOne(BasicDBObjectBuilder.start("_id", file.getId()).get());
        assertNotNull(findOne);

        ByteArrayOutputStream out = new ByteArrayOutputStream(32 * 1024);
        new BytesCopier(findOne.getInputStream(), out).transfer(true);
        assertEquals(LOREM_IPSUM, out.toString());
    }

    @Test
    public void testFactoredGridFS()
            throws IOException {

        String bucket = "refactored";
        me.lightspeed7.mongofs.gridfs.GridFS gridFS = new me.lightspeed7.mongofs.gridfs.GridFS(database, bucket);
        me.lightspeed7.mongofs.gridfs.GridFSInputFile file = gridFS.createFile("refactoredGridFS.txt");
        file.put(MongoFileConstants.chunkCount.toString(), 0);
        file.put(MongoFileConstants.compressionRatio.toString(), 0.0d);
        file.put(MongoFileConstants.compressedLength.toString(), 0);
        file.put("aliases", Arrays.asList("one", "two", "three"));
        file.setMetaData(new BasicDBObject("key", "value"));

        try (OutputStream stream = file.getOutputStream()) {
            byte[] bytes = LOREM_IPSUM.getBytes();
            stream.write(bytes);
        }

        System.out.println("Refactored GridFS (3.0.x)");
        System.out.println("==============================");
        System.out.println(String.format("id = %s, filepath = %s", file.getId(), file.getFilename()));
        System.out.println("==============================");
        System.out.println(JSONHelper.prettyPrint(file.toString()));
        System.out.println("======");
        dumpChunks(bucket, file.getId(), System.out);
        System.out.println("==============================");
        System.out.println();

        // md5 validation
        try {
            file.validate();
        } catch (MongoException e) {
            fail(e.getMessage());
        }

        assertEquals(me.lightspeed7.mongofs.gridfs.GridFS.DEFAULT_CHUNKSIZE, file.getChunkSize());
        assertEquals(1, file.numChunks());

        me.lightspeed7.mongofs.gridfs.GridFSDBFile findOne = gridFS.findOne(BasicDBObjectBuilder.start("_id",
                file.getId()).get());
        assertNotNull(findOne);

        ByteArrayOutputStream out = new ByteArrayOutputStream(32 * 1024);
        new BytesCopier(findOne.getInputStream(), out).transfer(true);
        assertEquals(LOREM_IPSUM, out.toString());

    }

    @Test
    // @Ignore
    public void testMongoFS()
            throws IOException {

        String bucket = "mongofs";
        MongoFileStore store = new MongoFileStore(database, bucket);
        MongoFileWriter writer = store.createNew("mongoFS.txt", "text/plain");
        MongoFile file = writer.getMongoFile();

        file.put("aliases", Arrays.asList("one", "two", "three"));
        file.setMetaData(new BasicDBObject("key", "value"));

        writer.write(new ByteArrayInputStream(LOREM_IPSUM.getBytes()));

        System.out.println("MongoFS (0.x)");
        System.out.println("==============================");
        System.out.println(String.format("url= %s", file.getURL().toString()));
        System.out.println("==============================");
        System.out.println(JSONHelper.prettyPrint(file.toString()));
        System.out.println("======");
        dumpChunks(bucket, file.getId(), System.out);
        System.out.println("==============================");
        System.out.println();

        // md5 validation
        try {
            file.validate();
        } catch (MongoException e) {
            fail(e.getMessage());
        }

        assertEquals(store.getChunkSize(), file.getChunkSize());
        assertEquals(1, file.getChunkCount());

        // me.lightspeed7.mongofs.gridfs.GridFSDBFile findOne = store.findOne(BasicDBObjectBuilder.start("_id",
        // file.getId()).get());
        // assertNotNull(findOne);
        //
        // ByteArrayOutputStream out = new ByteArrayOutputStream(32 * 1024);
        // new BytesCopier(findOne.getInputStream(), out).transfer(true);
        // assertEquals(LOREM_IPSUM, out.toString());

    }

    // internal
    private void dumpChunks(String bucket, Object id, PrintStream out) {

        DBCollection collection = database.getCollection(bucket + ".chunks");
        DBCursor cursor = collection.find(new BasicDBObject("files_id", id)).sort(new BasicDBObject("n", 1));

        while (cursor.hasNext()) {
            DBObject current = cursor.next();
            out.println(current.toString());
        }
    }

}
