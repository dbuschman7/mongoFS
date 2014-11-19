package me.lightspeed7.mongofs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import me.lightspeed7.mongofs.crypto.BasicCrypto;
import me.lightspeed7.mongofs.url.StorageFormat;
import me.lightspeed7.mongofs.util.BytesCopier;
import me.lightspeed7.mongofs.util.ChunkSize;

import org.junit.BeforeClass;
import org.junit.Ignore;
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
    @Ignore
    public void testBasicUncompressedRoundTrip() throws IOException {

        doRoundTrip("mongofs", "loremIpsum.txt", MongoFileStoreConfig.DEFAULT_CHUNKSIZE, false, false);
    }

    @Test
    @Ignore
    public void testBasicCompressedRoundTrip() throws IOException {

        doRoundTrip("mongofs", "loremIpsum.txt", MongoFileStoreConfig.DEFAULT_CHUNKSIZE, true, false);
    }

    @Test
    @Ignore
    public void testLotsOfChunksUncompressedRoundTrip() throws IOException {

        doRoundTrip("mongofs", "loremIpsum.txt", ChunkSize.tiny_4K, false, false);
    }

    @Test
    @Ignore
    public void testLotsOfChunksCompressedRoundTrip() throws IOException {

        doRoundTrip("mongofs", "loremIpsum.txt", ChunkSize.tiny_4K, true, false);
    }

    @Test
    public void testLotsOfChunksEncryptedRoundTrip() throws IOException {

        doRoundTrip("mongofs", "loremIpsum.txt", ChunkSize.tiny_4K, false, true);
    }

    @Test
    public void testLotsOfChunksEncryptedAndCompressedRoundTrip() throws IOException {

        doRoundTrip("mongofs", "loremIpsum.txt", ChunkSize.tiny_4K, true, true);
    }

    @Test
    public void testSingleChunkEncryptedAndCompressedRoundTrip() throws IOException {

        doRoundTrip("mongofs", "loremIpsum.txt", ChunkSize.large_1M, true, true);
    }

    @Test
    public void testUpload() throws IOException {

        MongoFileStoreConfig config = MongoFileStoreConfig.builder()//
                .bucket("mongofs").chunkSize(ChunkSize.medium_256K)//
                .enableCompression(true).enableEncryption(new BasicCrypto())//
                .writeConcern(WriteConcern.SAFE) //
                .build();
        MongoFileStore store = new MongoFileStore(database, config);

        ByteArrayInputStream in = new ByteArrayInputStream(LoremIpsum.LOREM_IPSUM.getBytes());
        MongoFile mongoFile = store.upload("loremIpsum.txt", "test/plain", null, false, in);
        assertNotNull(mongoFile);

        assertEquals(32087, mongoFile.getLength());

    }

    //
    // internal
    // /////////////////

    private void doRoundTrip(final String bucket, final String filename, final ChunkSize chunkSize, final boolean compress,
            final boolean encrypt) throws IOException {

        MongoFileStoreConfig config = MongoFileStoreConfig.builder()//
                .bucket(bucket).chunkSize(chunkSize)//
                .enableCompression(compress).enableEncryption(encrypt ? new BasicCrypto(chunkSize) : null)//
                .writeConcern(WriteConcern.SAFE) //
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
        if (compress && encrypt) {
            assertEquals(StorageFormat.ECRYPTED_GZIP.getCode(), mongoFile.get(MongoFileConstants.format)); // verify compression
            assertNotNull(mongoFile.get(MongoFileConstants.storage)); // verify compression
            assertNotNull(mongoFile.get(MongoFileConstants.ratio)); // verify compression
        }
        else if (compress) {
            assertEquals(StorageFormat.GZIPPED.getCode(), mongoFile.get(MongoFileConstants.format)); // verify compression
            assertNotNull(mongoFile.get(MongoFileConstants.storage)); // verify compression
            assertNotNull(mongoFile.get(MongoFileConstants.ratio)); // verify compression
        }
        else if (encrypt) {
            assertEquals(StorageFormat.ENCRYPTED.getCode(), mongoFile.get(MongoFileConstants.format)); // verify encryption
            assertNotNull(mongoFile.get(MongoFileConstants.storage)); // verify encryption
            assertNotNull(mongoFile.get(MongoFileConstants.ratio)); // verify encryption sets its ratio
        }
        else {
            assertNull(mongoFile.get(MongoFileConstants.storage)); // verify no compression
            assertNull(mongoFile.get(MongoFileConstants.format)); // verify no compression
            assertNull(mongoFile.get(MongoFileConstants.ratio)); // verify no compression
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
