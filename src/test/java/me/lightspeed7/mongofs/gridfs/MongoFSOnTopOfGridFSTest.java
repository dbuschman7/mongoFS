package me.lightspeed7.mongofs.gridfs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;

import me.lightspeed7.mongofs.LoremIpsum;
import me.lightspeed7.mongofs.MongoFile;
import me.lightspeed7.mongofs.MongoFileConstants;
import me.lightspeed7.mongofs.MongoFileReader;
import me.lightspeed7.mongofs.MongoFileStore;
import me.lightspeed7.mongofs.MongoFileStoreConfig;
import me.lightspeed7.mongofs.MongoTestConfig;
import me.lightspeed7.mongofs.util.BytesCopier;
import me.lightspeed7.mongofs.util.JSONHelper;

import org.bson.types.ObjectId;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mongodb.Document;
import org.mongodb.MongoCollection;
import org.mongodb.MongoCollectionOptions;
import org.mongodb.MongoCursor;
import org.mongodb.MongoDatabase;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;

public class MongoFSOnTopOfGridFSTest {

    private static final String bucket = "original";

    private static final String DB_NAME = "MongoFSTest-onTop";

    private static Document ID = new Document(MongoFileConstants._id.name(), new ObjectId());

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
    public void doTheTest() throws IOException {

        createOriginalGridFSFile();
        verifyReadFromRefactoredGridFS();
        verifyReadFromMongoFS();

    }

    public Object createOriginalGridFSFile() throws IOException {

        com.mongodb.gridfs.GridFS gridFS = new com.mongodb.gridfs.GridFS(database.getSurrogate(), bucket);
        com.mongodb.gridfs.GridFSInputFile file = gridFS.createFile("originalGridFS.txt");
        file.setId(ID.get(MongoFileConstants._id.toString()));
        file.put("aliases", Arrays.asList("one", "two", "three"));
        file.put(MongoFileConstants.contentType.toString(), "text/plain");
        file.setMetaData(new BasicDBObject("key", "value"));

        OutputStream stream = file.getOutputStream();
        try {
            byte[] bytes = LoremIpsum.LOREM_IPSUM.getBytes();
            stream.write(bytes);
        } finally {
            stream.close();
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

        com.mongodb.gridfs.GridFSDBFile findOne = gridFS.findOne(ID.getSurrogate());
        assertNotNull(findOne);

        ByteArrayOutputStream out = new ByteArrayOutputStream(32 * 1024);
        new BytesCopier(findOne.getInputStream(), out).transfer(true);
        assertEquals(LoremIpsum.LOREM_IPSUM, out.toString());
        return ID;
    }

    public void verifyReadFromRefactoredGridFS() throws IOException {

        me.lightspeed7.mongofs.gridfs.GridFS gridFS = new me.lightspeed7.mongofs.gridfs.GridFS(database.getSurrogate(), bucket);
        me.lightspeed7.mongofs.gridfs.GridFSDBFile findOne = gridFS.findOne(ID.getSurrogate());
        assertNotNull(findOne);

        ByteArrayOutputStream out = new ByteArrayOutputStream(32 * 1024);
        new BytesCopier(findOne.getInputStream(), out).transfer(true);
        assertEquals(LoremIpsum.LOREM_IPSUM, out.toString());

        System.out.println("Passed - RefactoredGridFS");

    }

    public void verifyReadFromMongoFS() throws IOException {

        MongoFileStore store = new MongoFileStore(database, MongoFileStoreConfig.builder().bucket(bucket).build());
        MongoFile findOne = store.findOne((ObjectId) ID.get(MongoFileConstants._id.name()));

        ByteArrayOutputStream out = new ByteArrayOutputStream(32 * 1024);
        new BytesCopier(new MongoFileReader(store, findOne).getInputStream(), out).transfer(true);
        assertEquals(LoremIpsum.LOREM_IPSUM, out.toString());

        System.out.println("Passed - MongoFS");
    }

    //
    // internal
    private void dumpChunks(String bucket, Object id, PrintStream out) {

        MongoCollection<Document> collection = database.getCollection(bucket + ".chunks", MongoCollectionOptions.builder().build());
        MongoCursor<Document> cursor = collection.find(new Document("files_id", id)).sort(new Document("n", 1)).get();

        while (cursor.hasNext()) {
            Document current = cursor.next();
            out.println(current.toString());
        }
    }
}
