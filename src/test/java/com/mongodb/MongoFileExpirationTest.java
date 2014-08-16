package com.mongodb;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Date;

import me.lightspeed7.mongofs.LoremIpsum;
import me.lightspeed7.mongofs.MongoFileCursor;
import me.lightspeed7.mongofs.MongoFileStore;
import me.lightspeed7.mongofs.MongoFileStoreConfig;
import me.lightspeed7.mongofs.MongoFileWriter;
import me.lightspeed7.mongofs.MongoTestConfig;
import me.lightspeed7.mongofs.util.TimeMachine;

import org.junit.BeforeClass;
import org.junit.Test;

public class MongoFileExpirationTest implements LoremIpsum {

    private static final String DB_NAME = "MongoFSTest-expiration";

    private static DB database;

    private static MongoClient mongoClient;
    private static MongoFileStore store;

    // initializer
    @BeforeClass
    public static void initial() throws IllegalArgumentException, IOException, InterruptedException {

        mongoClient = MongoTestConfig.construct();

        mongoClient.dropDatabase(DB_NAME);
        database = mongoClient.getDB(DB_NAME);

        MongoFileStoreConfig config = new MongoFileStoreConfig("expire");
        config.setWriteConcern(WriteConcern.SAFE);
        store = new MongoFileStore(database, config);

        Thread.sleep(2000);
    }

    @Test
    public void test() throws IOException {

        long now = System.currentTimeMillis();

        createTempFile(store, "/foo/bar1.txt", "text/plain", TimeMachine.now().backward(2).days().inTime());
        createTempFile(store, "/foo/bar1.txt", "text/plain", TimeMachine.from(now).forward(5).seconds().inTime());

        MongoFileCursor cursor = store.query().find("/foo/bar1.txt");
        assertTrue(Math.abs(now - (2 * 24 * 60 * 60 * 1000) - cursor.next().getExpiresAt().getTime()) <= 1);
        assertTrue(Math.abs(now + (5 * 1000) - cursor.next().getExpiresAt().getTime()) <= 1);
    }

    @Test
    public void testExpiresInThePast() throws IOException, InterruptedException {

        //
        Date when = new Date();
        Thread.sleep(200);
        assertTrue(when.before(new Date()));
    }

    //
    // internal
    // //////////////////
    private void createTempFile(MongoFileStore store, String filename, String mediaType, Date expiresAt) throws IOException {

        MongoFileWriter writer = store.createNew(filename, mediaType, expiresAt, true);
        writer.write(new ByteArrayInputStream(LOREM_IPSUM.getBytes()));
    }
}
