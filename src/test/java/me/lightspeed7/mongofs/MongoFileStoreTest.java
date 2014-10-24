package me.lightspeed7.mongofs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

import me.lightspeed7.mongofs.crypto.BasicCrypto;
import me.lightspeed7.mongofs.url.MongoFileUrl;
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

    private void doRoundTrip(final String bucket, final String filename, final ChunkSize chunkSize, final boolean compress,
            final boolean encrypt) throws IOException {

        MongoFileStoreConfig config = MongoFileStoreConfig.builder()//
                .bucket(bucket).chunkSize(chunkSize)//
                .enableCompression(compress).enableEncryption(encrypt ? new BasicCrypto(chunkSize) : null)//
                .writeConcern(WriteConcern.SAFE) //
                .build();
        MongoFileStore store = new MongoFileStore(database, config);

        assertEquals(true, store.validateConnection());

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
            assertNotNull(mongoFile.get(MongoFileConstants.storage)); // verify compression
            assertEquals(MongoFileUrl.GZIPPED, mongoFile.get(MongoFileConstants.format)); // verify compression
            assertNotNull(mongoFile.get(MongoFileConstants.ratio)); // verify compression
        }
        else if (encrypt) {
            assertEquals(MongoFileUrl.ENCRYPTED, mongoFile.get(MongoFileConstants.format)); // verify encryption
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

    }

    @Test
    public void testUpload() throws IOException {
        MongoFileStore store = new MongoFileStore(database, MongoFileStoreConfig.builder().build());
        assertNotNull(store.toString());

        MongoFile file = store.upload(LoremIpsum.getFile(), "text/plain");
        assertNotNull(file);
        assertEquals(LoremIpsum.LOREM_IPSUM.length(), file.getLength());
        assertTrue(store.exists(file.getURL()));
        assertTrue(store.exists(file.getId()));

        MongoFile file2 = store.findOne(file.getURL().getUrl());
        assertNotNull(file2);

    }

    @Test(expected = IllegalArgumentException.class)
    public void testException1() throws IOException {

        MongoFileStore store = new MongoFileStore(database, MongoFileStoreConfig.builder().build());
        store.createNew(null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testException2() throws IOException {

        MongoFileStore store = new MongoFileStore(database, MongoFileStoreConfig.builder().build());
        store.createNew("", null);
    }

    @Test(expected = IllegalStateException.class)
    public void testException3() throws IOException {

        MongoFileStore store = new MongoFileStore(database, MongoFileStoreConfig.builder().enableCompression(false).build());
        store.createNew("", "", null, true);
    }

    @Test(expected = IllegalStateException.class)
    public void testException4() throws IOException {

        MongoFileStore store = new MongoFileStore(database, MongoFileStoreConfig.builder()//
                .enableCompression(false).enableEncryption(new BasicCrypto()).build());
        store.createNew("", "", null, true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testException5() throws IOException {

        MongoFileStore store = new MongoFileStore(database, MongoFileStoreConfig.builder().build());
        store.upload(null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testException6() throws IOException {

        MongoFileStore store = new MongoFileStore(database, MongoFileStoreConfig.builder().build());
        store.upload(LoremIpsum.getFile(), null);
    }

    @Test(expected = IllegalStateException.class)
    public void testException8() throws IOException {

        MongoFileStore store = new MongoFileStore(database, MongoFileStoreConfig.builder().enableCompression(false).build());
        store.upload(LoremIpsum.getFile(), "text/plain", true, null);
    }

    @Test(expected = IllegalStateException.class)
    public void testException9() throws IOException {

        MongoFileStore store = new MongoFileStore(database, MongoFileStoreConfig.builder()//
                .enableCompression(false).enableEncryption(new BasicCrypto()).build());
        store.upload(LoremIpsum.getFile(), "text/plain", true, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testException10() throws IOException {

        MongoFileStore store = new MongoFileStore(database, MongoFileStoreConfig.builder().build());
        store.getManifest((MongoFileUrl) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testException11() throws IOException {

        MongoFileStore store = new MongoFileStore(database, MongoFileStoreConfig.builder().build());
        store.getManifest((MongoFile) null);
    }

    @Test(expected = IllegalStateException.class)
    public void testException12() throws IOException {

        MongoFileStore store = new MongoFileStore(database, MongoFileStoreConfig.builder().build());
        MongoFile file = store.upload(LoremIpsum.getFile(), "text/plain");

        store.getManifest(file);
    }

    @Test(expected = IllegalStateException.class)
    public void testException13() throws IOException {

        MongoFileStore store = new MongoFileStore(database, MongoFileStoreConfig.builder().build());
        MongoFile file = store.upload(LoremIpsum.getFile(), "text/plain");

        store.getManifest(file.getURL());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testException14() throws IOException {

        MongoFileStore store = new MongoFileStore(database, MongoFileStoreConfig.builder().build());
        store.findOne((URL) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testException15() throws IOException {

        MongoFileStore store = new MongoFileStore(database, MongoFileStoreConfig.builder().build());
        store.findOne((MongoFileUrl) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testException16() throws IOException {

        MongoFileStore store = new MongoFileStore(database, MongoFileStoreConfig.builder().build());
        store.exists((MongoFileUrl) null);
    }

}
