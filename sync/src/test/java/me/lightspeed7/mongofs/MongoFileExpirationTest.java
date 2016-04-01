package me.lightspeed7.mongofs;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.NoSuchElementException;

import me.lightspeed7.mongofs.util.TimeMachine;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mongodb.Document;
import org.mongodb.MongoDatabase;
import org.mongodb.MongoView;

import com.mongodb.MongoClient;

public class MongoFileExpirationTest {

    private static final String DB_NAME = "MongoFSTest-fileExpiration";

    private static MongoDatabase database;

    private static MongoClient mongoClient;

    private static MongoFileStore store;

    // initializer
    @BeforeClass
    public static void initial() {

        mongoClient = MongoTestConfig.construct();

        mongoClient.dropDatabase(DB_NAME);
        database = new MongoDatabase(mongoClient.getDB(DB_NAME));

        store = new MongoFileStore(database, MongoFileStoreConfig.builder().build());

    }

    @Test
    public void testASyncDelete() throws IOException {
        MongoFile file = store.upload(LoremIpsum.getFile(), "text/plain");
        assertNotNull(file);
        assertTrue(file.getBoolean(MongoFileConstants.deleted, true)); // should return default

        store.remove(file, true);

        MongoView<Document> find = store.getFilesCollection().find(new Document("_id", file.getId()));
        Document document = find.getOne();
        assertNotNull(document);
        assertTrue(document.containsKey(MongoFileConstants.deleted.name()));
        assertTrue(document.getBoolean(MongoFileConstants.deleted.name()));

        assertNotNull(document.getDate(MongoFileConstants.expireAt.name()));
    }

    @Test(expected = NoSuchElementException.class)
    public void testSyncDelete() throws IOException {
        MongoFile file = store.upload(LoremIpsum.getFile(), "text/plain");
        assertNotNull(file);

        store.remove(file);

        MongoView<Document> find = store.getFilesCollection().find(new Document("_id", file.getId()));
        find.getOne(); // should throw NoSuchElementException
    }

    @Test
    public void testExpireFile() throws IOException {
        MongoFile file = store.upload(LoremIpsum.getFile(), "text/plain");
        assertNotNull(file);

        store.expireFile(file, TimeMachine.now().forward(1).minutes().inTime());

        MongoView<Document> find = store.getFilesCollection().find(new Document("_id", file.getId()));
        Document document = find.getOne();
        assertNotNull(document);
        assertTrue(document.containsKey(MongoFileConstants.deleted.name()));
        assertFalse(document.getBoolean(MongoFileConstants.deleted.name()));

        assertNotNull(document.getDate(MongoFileConstants.expireAt.name()));
    }
}
