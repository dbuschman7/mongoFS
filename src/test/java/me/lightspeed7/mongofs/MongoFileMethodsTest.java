package me.lightspeed7.mongofs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mongodb.MongoDatabase;

import com.mongodb.MongoClient;

public class MongoFileMethodsTest {

    private static final String DB_NAME = "MongoFSTest-fileStore";

    private static MongoDatabase database;

    private static MongoClient mongoClient;

    private static MongoFileStore store;

    private static MongoFile file;

    private static MongoFile newFile;

    // initializer
    @BeforeClass
    public static void initial() throws IOException {

        mongoClient = MongoTestConfig.construct();

        mongoClient.dropDatabase(DB_NAME);
        database = new MongoDatabase(mongoClient.getDB(DB_NAME));

        store = new MongoFileStore(database, MongoFileStoreConfig.builder().build());

        file = store.upload(LoremIpsum.getFile(), "text/plain");

        MongoFileWriter writer = store.createNew("fileName", "text/plain");
        newFile = writer.getMongoFile();

    }

    @Test
    public void testMongoFileMethods() throws IOException {
        assertNotNull(file);

        file.setAliases(null);
        assertNull(file.getAliases());

        assertEquals("text/plain", file.get(MongoFileConstants.contentType.name()));
        assertEquals("text/plain", file.getString(MongoFileConstants.contentType, ""));

        assertTrue(file.getBoolean(MongoFileConstants.chunkSize, false));

        assertNotNull(file.getObjectId(MongoFileConstants._id, null));
        assertTrue(file.isCompressed());
        assertFalse(file.isEncrypted());

        assertNotNull(file.toString());

        assertNull(file.getMetaData());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testException3() throws IOException {

        newFile.put((String) null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testException4() throws IOException {

        newFile.put((MongoFileConstants) null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testException5() throws IOException {

        newFile.get((String) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testException6() throws IOException {

        newFile.get((MongoFileConstants) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testException7() throws IOException {

        newFile.getInt((MongoFileConstants) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testException8() throws IOException {

        newFile.getLong((MongoFileConstants) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testException9() throws IOException {

        new MongoFileReader(null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testException10() throws IOException {

        new MongoFileReader(store, null);
    }
}
