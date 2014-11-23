package me.lightspeed7.mongofs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;

import me.lightspeed7.mongofs.util.BytesCopier;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mongodb.Document;
import org.mongodb.MongoCollection;
import org.mongodb.MongoCollectionOptions;
import org.mongodb.MongoCursor;
import org.mongodb.MongoDatabase;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;

public class StorageComparisonTest {

    private static final String DB_NAME = "MongoFSTest-comparison";

    private static MongoDatabase database;

    private static MongoClient mongoClient;

    // initializer
    @BeforeClass
    public static void initial() {

        mongoClient = MongoTestConfig.construct();

        mongoClient.dropDatabase(DB_NAME);
        database = new MongoDatabase(mongoClient.getDB(DB_NAME));

    }

    @Test
    public void testOriginalGridFS() throws IOException {

        String bucket = "original";
        com.mongodb.gridfs.GridFS gridFS = new com.mongodb.gridfs.GridFS(database.getSurrogate(), bucket);
        com.mongodb.gridfs.GridFSInputFile file = gridFS.createFile("originalGridFS.txt");
        file.put(MongoFileConstants.chunkCount.toString(), 0);
        file.put("aliases", Arrays.asList("one", "two", "three"));
        file.setMetaData(new BasicDBObject("key", "value"));

        OutputStream stream = file.getOutputStream();
        try {
            byte[] bytes = LoremIpsum.LOREM_IPSUM.getBytes();
            stream.write(bytes);
        } finally {
            stream.close();
        }

        // System.out.println("Original GridFS (2.11.4)");
        // System.out.println("==============================");
        // System.out.println(String.format("id = %s, filepath = %s", file.getId(), file.getFilename()));
        // System.out.println("==============================");
        // System.out.println(JSONHelper.prettyPrint(file.toString()));
        // System.out.println("======");
        // dumpChunks(bucket, file.getId(), System.out);
        // System.out.println("==============================");
        // System.out.println();

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
        assertEquals(LoremIpsum.LOREM_IPSUM, out.toString());
    }

    @Test
    public void testFactoredGridFS() throws IOException {

        String bucket = "refactored";
        me.lightspeed7.mongofs.gridfs.GridFS gridFS = new me.lightspeed7.mongofs.gridfs.GridFS(database.getSurrogate(), bucket);
        me.lightspeed7.mongofs.gridfs.GridFSInputFile file = gridFS.createFile("refactoredGridFS.txt");
        file.put(MongoFileConstants.chunkCount.toString(), 0);
        file.put(MongoFileConstants.ratio.toString(), 0.0d);
        file.put(MongoFileConstants.storage.toString(), 0);
        file.put("aliases", Arrays.asList("one", "two", "three"));
        file.setMetaData(new BasicDBObject("key", "value"));

        OutputStream stream = file.getOutputStream();
        try {
            byte[] bytes = LoremIpsum.LOREM_IPSUM.getBytes();
            stream.write(bytes);
        } finally {
            stream.close();
        }

        // System.out.println("Refactored GridFS (3.0.x)");
        // System.out.println("==============================");
        // System.out.println(String.format("id = %s, filepath = %s", file.getId(), file.getFilename()));
        // System.out.println("==============================");
        // System.out.println(JSONHelper.prettyPrint(file.toString()));
        // System.out.println("======");
        // dumpChunks(bucket, file.getId(), System.out);
        // System.out.println("==============================");
        // System.out.println();

        // md5 validation
        try {
            file.validate();
        } catch (MongoException e) {
            fail(e.getMessage());
        }

        assertEquals(me.lightspeed7.mongofs.gridfs.GridFS.DEFAULT_CHUNKSIZE, file.getChunkSize());
        assertEquals(1, file.numChunks());

        me.lightspeed7.mongofs.gridfs.GridFSDBFile findOne = gridFS.findOne(BasicDBObjectBuilder.start("_id", file.getId()).get());
        assertNotNull(findOne);

        ByteArrayOutputStream out = new ByteArrayOutputStream(32 * 1024);
        new BytesCopier(findOne.getInputStream(), out).transfer(true);
        assertEquals(LoremIpsum.LOREM_IPSUM, out.toString());

    }

    @Test
    // @Ignore
    public void testMongoFS() throws IOException {

        String bucket = "mongofs";
        MongoFileStore store = new MongoFileStore(database, MongoFileStoreConfig.builder().bucket(bucket).build());
        MongoFileWriter writer = store.createNew("mongoFS.txt", "text/plain");
        assertNotNull(writer.toString());
        MongoFile file = writer.getMongoFile();

        file.put("aliases", Arrays.asList("one", "two", "three"));
        file.setInMetaData("key1", "value1");
        file.setInMetaData("key2", "value2");

        writer.write(new ByteArrayInputStream(LoremIpsum.LOREM_IPSUM.getBytes()));

        // System.out.println("MongoFS (0.x)");
        // System.out.println("==============================");
        // System.out.println(String.format("url= %s", file.getURL().toString()));
        // System.out.println("==============================");
        // System.out.println(JSONHelper.prettyPrint(file.toString()));
        // System.out.println("======");
        // dumpChunks(bucket, file.getId(), System.out);
        // System.out.println("==============================");
        // System.out.println();

        // md5 validation
        try {
            file.validate();
        } catch (MongoException e) {
            fail(e.getMessage());
        }

        assertEquals(store.getChunkSize().getChunkSize(), file.getChunkSize());
        assertEquals(1, file.getChunkCount());
        assertNotNull(file.getUploadDate());

        // the id is always a generated UUID, thus test for everything else to be correct
        assertTrue(file.getURL().toString().startsWith("mongofile:gz:mongoFS.txt?"));
        assertTrue(file.getURL().toString().endsWith("#text/plain"));

        MongoFile findOne = store.findOne(file.getId());
        assertNotNull(findOne);
        assertEquals("value1", findOne.getMetaData().get("key1"));
        assertEquals("value2", findOne.getMetaData().get("key2"));

        ByteArrayOutputStream out = new ByteArrayOutputStream(32 * 1024);
        MongoFileReader reader = new MongoFileReader(store, file);
        assertNotNull(reader.toString());
        new BytesCopier(reader.getInputStream(), out).transfer(true);
        assertEquals(LoremIpsum.LOREM_IPSUM, out.toString());
        assertNotNull(reader.getMongoFile());
    }

    // internal
    protected void dumpChunks(final String bucket, final Object id, final PrintStream out) {

        MongoCollection<Document> collection = database.getCollection(bucket + ".chunks", MongoCollectionOptions.builder().build());
        MongoCursor<Document> cursor = collection.find(new Document("files_id", id)).sort(new Document("n", 1)).get();

        while (cursor.hasNext()) {
            Document current = cursor.next();
            out.println(current.toString());
        }
    }
}
