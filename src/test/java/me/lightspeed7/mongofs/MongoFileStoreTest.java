package me.lightspeed7.mongofs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;

import me.lightspeed7.mongofs.util.BytesCopier;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mongodb.MongoDatabase;

import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;

public class MongoFileStoreTest {

    private static final String DB_NAME = "MongoFSTest-fileStore";

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
    public void testBasicUncompressedRoundTrip() throws IllegalArgumentException, IOException {

        doRoundTrip("mongofs", "loremIpsum.txt", MongoFileStoreConfig.DEFAULT_CHUNKSIZE, false);
    }

    @Test
    public void testBasicCompressedRoundTrip() throws IllegalArgumentException, IOException {

        doRoundTrip("mongofs", "loremIpsum.txt", MongoFileStoreConfig.DEFAULT_CHUNKSIZE, true);
    }

    @Test
    public void testLotsOfChunksUncompressedRoundTrip() throws IllegalArgumentException, IOException {

        doRoundTrip("mongofs", "loremIpsum.txt", ChunkSize.tiny_4K, false);
    }

    @Test
    public void testLotsOfChunksCompressedRoundTrip() throws IllegalArgumentException, IOException {

        doRoundTrip("mongofs", "loremIpsum.txt", ChunkSize.tiny_4K, true);
    }

    //
    // internal
    // /////////////////

    private void doRoundTrip(String bucket, String filename, ChunkSize chunkSize, boolean compress) throws IOException,
            MalformedURLException {

        MongoFileStoreConfig config = MongoFileStoreConfig.builder()//
                .bucket(bucket).chunkSize(chunkSize).writeConcern(WriteConcern.SAFE) //
                .build();
        MongoFileStore store = new MongoFileStore(database, config);

        MongoFileWriter writer = store.createNew(filename, "text/plain", null, compress);
        ByteArrayInputStream in = new ByteArrayInputStream(LoremIpsum.LOREM_IPSUM.getBytes());
        OutputStream out = writer.getOutputStream();
        try {
            new BytesCopier(in, out).transfer(true);
        } finally {
            out.close();
        }

        // verify it exists
        MongoFile mongoFile = writer.getMongoFile();
        assertTrue(store.exists(mongoFile.getURL()));

        // read a file
        assertEquals(compress, mongoFile.getURL().isStoredCompressed());
        assertEquals(LoremIpsum.LOREM_IPSUM.length(), mongoFile.getLength());
        if (compress) {
            assertNotNull(mongoFile.get(MongoFileConstants.compressedLength)); // verify compression
            assertNotNull(mongoFile.get(MongoFileConstants.compressionFormat)); // verify compression
            assertNotNull(mongoFile.get(MongoFileConstants.compressionRatio)); // verify compression
        }
        else {
            assertNull(mongoFile.get(MongoFileConstants.compressedLength)); // verify no compression
            assertNull(mongoFile.get(MongoFileConstants.compressionFormat)); // verify no compression
            assertNull(mongoFile.get(MongoFileConstants.compressionRatio)); // verify no compression
        }

        ByteArrayOutputStream out2 = new ByteArrayOutputStream(32 * 1024);
        new BytesCopier(new MongoFileReader(store, mongoFile).getInputStream(), out2).transfer(true);
        assertEquals(LoremIpsum.LOREM_IPSUM, out2.toString());

        // remove a file
        // store.remove(mongoFile, true); // flag delete

        // verify it does not exist
        // assertFalse(store.exists(mongoFile.getURL()));
    }

}
