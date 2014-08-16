package me.lightspeed7.mongofs.harness;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;

import me.lightspeed7.mongofs.MongoFile;
import me.lightspeed7.mongofs.MongoFileStore;
import me.lightspeed7.mongofs.MongoFileStoreConfig;
import me.lightspeed7.mongofs.MongoTestConfig;

import org.junit.BeforeClass;
import org.junit.Test;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;

public class TestHarness {

    private static final String DB_NAME = "MongoFSTest-harness";

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
    public void test() throws IllegalArgumentException, IOException {

        MongoFileStoreConfig config = new MongoFileStoreConfig("xml");
        config.setWriteConcern(WriteConcern.SAFE);
        config.setReadPreference(ReadPreference.primary());
        MongoFileStore store = new MongoFileStore(database, config);

        File file = new File("/Users/dbusch/Documents/Gasplant", "GasStatementData_J24_20131101_20131001_4_1.xml");

        MongoFile mongoFile = store.upload(file, "application/xml");
        assertNotNull(mongoFile);

        mongoFile.validate();
    }
}
