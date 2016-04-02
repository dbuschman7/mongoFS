package me.lightspeed7.mongofs.zip;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipOutputStream;

import me.lightspeed7.mongofs.FileStoreHelper;
import me.lightspeed7.mongofs.LoremIpsum;
import me.lightspeed7.mongofs.MongoFile;
import me.lightspeed7.mongofs.MongoFileConstants;
import me.lightspeed7.mongofs.MongoFileStore;
import me.lightspeed7.mongofs.MongoFileStoreConfig;
import me.lightspeed7.mongofs.MongoFileWriter;
import me.lightspeed7.mongofs.MongoManifest;
import me.lightspeed7.mongofs.MongoTestConfig;
import me.lightspeed7.mongofs.url.MongoFileUrl;
import me.lightspeed7.mongofs.util.FileUtil;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mongodb.MongoDatabase;

import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;

public class ZipFileExpandedDeletionTest {

    private static final String TEST_ZIP = "./resources/test.zip";

    private static final String DB_NAME = "MongoFSTest-zipExpander";

    private static MongoFileStore store;

    // initializer
    @BeforeClass
    public static void initial() throws IOException {

        MongoClient mongoClient = MongoTestConfig.construct();

        mongoClient.dropDatabase(DB_NAME);
        MongoDatabase database = new MongoDatabase(mongoClient.getDB(DB_NAME));

        final File f = new File(TEST_ZIP);
        if (!f.exists()) {
            f.delete();
            final ZipOutputStream out = new ZipOutputStream(new FileOutputStream(f));
            try {
                ZipFileExpanderTest.createFile(out, "file1.txt", LoremIpsum.LOREM_IPSUM.getBytes());
                ZipFileExpanderTest.createFile(out, "file2.txt", LoremIpsum.LOREM_IPSUM.getBytes());
                ZipFileExpanderTest.createFile(out, "manifest.xml", ZipFileExpanderTest.XML.getBytes());
            } finally {
                if (out != null) {
                    out.close();
                }
            }
        }

        MongoFileStoreConfig config = MongoFileStoreConfig.builder()//
                .bucket("zipExpander-delete").chunkSize(MongoFileStoreConfig.DEFAULT_CHUNKSIZE)//
                .enableCompression(true)//
                .writeConcern(WriteConcern.SAFE) //
                .build();

        store = new MongoFileStore(database, config);

    }

    @Test
    public void testUploadAndDeleteASync() throws IOException {
        File file = new File(TEST_ZIP);
        String filename = file.getCanonicalPath();

        MongoFileWriter writer = store.createNew(filename, FileUtil.getContentType(filename));

        MongoManifest manifest = writer.uploadZipFile(new FileInputStream(file));
        assertNotNull(manifest);
        assertEquals(filename, manifest.getZip().getFilename());
        assertEquals(3, manifest.getFiles().size());

        MongoFileUrl url = manifest.getZip().getURL();
        store.remove(url, true);

        for (MongoFile mongoFile : manifest.getFiles()) {
            MongoFileUrl mongoFileUrl = mongoFile.getURL();
            assertNull(store.findOne(mongoFileUrl));
            MongoFile file2 = FileStoreHelper.internalFind(store, mongoFileUrl);
            assertNotNull(file2);
            assertTrue(file2.isDeleted());
            assertNotNull(file2.getDate(MongoFileConstants.expireAt, null));

        }

    }

    @Test
    public void testUploadAndDeleteSync() throws IOException {
        File file = new File(TEST_ZIP);
        String filename = file.getCanonicalPath();

        MongoFileWriter writer = store.createNew(filename, FileUtil.getContentType(filename));

        MongoManifest manifest = writer.uploadZipFile(new FileInputStream(file));
        assertNotNull(manifest);
        assertEquals(filename, manifest.getZip().getFilename());
        assertEquals(3, manifest.getFiles().size());

        MongoFileUrl url = manifest.getZip().getURL();
        store.remove(url);

        for (MongoFile mongoFile : manifest.getFiles()) {
            MongoFileUrl mongoFileUrl = mongoFile.getURL();
            assertNull(store.findOne(mongoFileUrl));
            assertNull(store.findOne(mongoFile.getURL().getMongoFileId()));

            MongoFile file2 = FileStoreHelper.internalFind(store, mongoFileUrl);
            assertNull(file2);
        }

    }
}
