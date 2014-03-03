package me.lightspeed7.mongofs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;

import me.lightspeed7.mongofs.util.BytesCopier;

import org.junit.BeforeClass;
import org.junit.Test;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;

public class MongoFileStoreTest implements LoremIpsum {

    private static final String DB_NAME = "MongoFSTest-fileStore";

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
    public void testBasicUncompressedRoundTrip()
            throws IllegalArgumentException, IOException {

        doRoundTrip("mongofs", "loremIpsum.txt", MongoFileStore.DEFAULT_CHUNKSIZE, false);
    }

    @Test
    public void testBasicCompressedRoundTrip()
            throws IllegalArgumentException, IOException {

        doRoundTrip("mongofs", "loremIpsum.txt", MongoFileStore.DEFAULT_CHUNKSIZE, true);
    }

    @Test
    public void testLotsOfChunksUncompressedRoundTrip()
            throws IllegalArgumentException, IOException {

        doRoundTrip("mongofs", "loremIpsum.txt", ChunkSize.tiny_4K, false);
    }

    @Test
    public void testLotsOfChunksCompressedRoundTrip()
            throws IllegalArgumentException, IOException {

        doRoundTrip("mongofs", "loremIpsum.txt", ChunkSize.tiny_4K, true);
    }

    //
    // internal
    // /////////////////

    private void doRoundTrip(String bucket, String filename, ChunkSize chunkSize, boolean compress)
            throws IOException, MalformedURLException {

        MongoFileStoreConfig config = new MongoFileStoreConfig(bucket);
        config.setChunkSize(chunkSize);
        config.setWriteConcern(WriteConcern.SAFE);
        MongoFileStore store = new MongoFileStore(database, config);

        MongoFileWriter writer = store.createNew(filename, "text/plain", null, compress);
        ByteArrayInputStream in = new ByteArrayInputStream(LOREM_IPSUM.getBytes());
        try (OutputStream out = writer.getOutputStream()) {
            new BytesCopier(in, out).transfer(true);
        }

        // verify it exists
        MongoFile file = writer.getMongoFile();
        assertTrue(store.exists(file.getURL()));

        // read a file
        MongoFile mongoFile = store.getFile(file.getURL());
        assertEquals(compress, mongoFile.getURL().isStoredCompressed());
        assertEquals(compress, mongoFile.getLength() != LoremIpsum.LOREM_IPSUM.length()); // verify compression

        ByteArrayOutputStream out = new ByteArrayOutputStream(32 * 1024);
        store.read(file, out, true);
        assertEquals(LOREM_IPSUM, out.toString());

        // remove a file
        store.remove(mongoFile, true); // flag delete

        // verify it does not exist
        assertFalse(store.exists(mongoFile.getURL()));
    }

}
