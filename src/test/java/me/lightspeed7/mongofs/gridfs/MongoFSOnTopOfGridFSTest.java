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
import me.lightspeed7.mongofs.MongoFileStore;
import me.lightspeed7.mongofs.MongoFileStoreConfig;
import me.lightspeed7.mongofs.MongoTestConfig;
import me.lightspeed7.mongofs.common.MongoFileConstants;
import me.lightspeed7.mongofs.util.BytesCopier;
import me.lightspeed7.mongofs.util.JSONHelper;

import org.bson.types.ObjectId;
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

public class MongoFSOnTopOfGridFSTest implements LoremIpsum {

    private static final String bucket = "original";

    private static final String DB_NAME = "MongoFSTest-onTop";

    private static DBObject ID = BasicDBObjectBuilder.start(MongoFileConstants._id.toString(), new ObjectId()).get();

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
    public void doTheTest() throws IOException {

        createOriginalGridFSFile();
        verifyReadFromRefactoredGridFS();
        verifyReadFromMongoFS();

    }

    public Object createOriginalGridFSFile() throws IOException {

        com.mongodb.gridfs.GridFS gridFS = new com.mongodb.gridfs.GridFS(database, bucket);
        com.mongodb.gridfs.GridFSInputFile file = gridFS.createFile("originalGridFS.txt");
        file.setId(ID.get(MongoFileConstants._id.toString()));
        file.put("aliases", Arrays.asList("one", "two", "three"));
        file.put(MongoFileConstants.contentType.toString(), "text/plain");
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

        com.mongodb.gridfs.GridFSDBFile findOne = gridFS.findOne(ID);
        assertNotNull(findOne);

        ByteArrayOutputStream out = new ByteArrayOutputStream(32 * 1024);
        new BytesCopier(findOne.getInputStream(), out).transfer(true);
        assertEquals(LOREM_IPSUM, out.toString());
        return ID;
    }

    public void verifyReadFromRefactoredGridFS() throws IOException {

        me.lightspeed7.mongofs.gridfs.GridFS gridFS = new me.lightspeed7.mongofs.gridfs.GridFS(database, bucket);
        me.lightspeed7.mongofs.gridfs.GridFSDBFile findOne = gridFS.findOne(ID);
        assertNotNull(findOne);

        ByteArrayOutputStream out = new ByteArrayOutputStream(32 * 1024);
        new BytesCopier(findOne.getInputStream(), out).transfer(true);
        assertEquals(LOREM_IPSUM, out.toString());

        System.out.println("Passed - RefactoredGridFS");

    }

    public void verifyReadFromMongoFS() throws IOException {

        MongoFileStore store = new MongoFileStore(database, new MongoFileStoreConfig(bucket));
        MongoFile findOne = store.query().findOne(ID);

        ByteArrayOutputStream out = new ByteArrayOutputStream(32 * 1024);
        store.read(findOne, out, true);
        assertEquals(LOREM_IPSUM, out.toString());

        System.out.println("Passed - MongoFS");
    }

    //
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
