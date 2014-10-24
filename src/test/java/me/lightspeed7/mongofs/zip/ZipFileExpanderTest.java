package me.lightspeed7.mongofs.zip;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import me.lightspeed7.mongofs.LoremIpsum;
import me.lightspeed7.mongofs.MongoFile;
import me.lightspeed7.mongofs.MongoFileConstants;
import me.lightspeed7.mongofs.MongoFileStore;
import me.lightspeed7.mongofs.MongoFileStoreConfig;
import me.lightspeed7.mongofs.MongoFileWriter;
import me.lightspeed7.mongofs.MongoManifest;
import me.lightspeed7.mongofs.MongoTestConfig;
import me.lightspeed7.mongofs.crypto.BasicCrypto;
import me.lightspeed7.mongofs.util.BytesCopier;
import me.lightspeed7.mongofs.util.FileUtil;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mongodb.MongoDatabase;

import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;

public class ZipFileExpanderTest {

    private static final String TEST_ZIP = "./src/test/resources/test.zip";

    private static final String DB_NAME = "MongoFSTest-zipExpander";

    private static MongoDatabase database;

    private static MongoClient mongoClient;

    // initializer
    @BeforeClass
    public static void initial() throws IOException {

        mongoClient = MongoTestConfig.construct();

        mongoClient.dropDatabase(DB_NAME);
        database = new MongoDatabase(mongoClient.getDB(DB_NAME));

        final File f = new File(TEST_ZIP);
        if (!f.exists()) {
            f.delete();
            final ZipOutputStream out = new ZipOutputStream(new FileOutputStream(f));
            try {
                createFile(out, "file1.txt", LoremIpsum.LOREM_IPSUM.getBytes());
                createFile(out, "file2.txt", LoremIpsum.LOREM_IPSUM.getBytes());
                createFile(out, "manifest.xml", XML.getBytes());
            } finally {
                if (out != null) {
                    out.close();
                }
            }
        }
    }

    @Test
    public void testUpload() throws IOException {
        MongoFileStoreConfig config = MongoFileStoreConfig.builder()//
                .bucket("mongofs").chunkSize(MongoFileStoreConfig.DEFAULT_CHUNKSIZE)//
                .enableCompression(true)//
                .writeConcern(WriteConcern.SAFE) //
                .build();

        runTests(new MongoFileStore(database, config), new File(TEST_ZIP));
    }

    @Test
    public void testUploadEncrypted() throws IOException {

        MongoFileStoreConfig config = MongoFileStoreConfig.builder()//
                .bucket("mongofs").chunkSize(MongoFileStoreConfig.DEFAULT_CHUNKSIZE)//
                .enableCompression(false) //
                .enableEncryption(new BasicCrypto())//
                .writeConcern(WriteConcern.SAFE) //
                .build();

        runTests(new MongoFileStore(database, config), new File(TEST_ZIP));
    }

    private void runTests(final MongoFileStore store, final File file) throws IOException {

        String filename = file.getAbsolutePath();

        MongoFileWriter writer = store.createNew(filename, FileUtil.getContentType(filename));

        MongoManifest manifest = writer.uploadZipFile(new FileInputStream(file));
        assertNotNull(manifest);
        assertEquals(filename, manifest.getZip().getFilename());
        assertEquals(3, manifest.getFiles().size());

        // verify correct data
        MongoFile zip = manifest.getZip();
        assertEquals(filename, zip.getFilename());
        assertEquals(0, zip.getLong(MongoFileConstants.storage, -1));
        assertTrue(zip.getDouble(MongoFileConstants.ratio, 0.0) > 0.0);
        assertTrue(zip.isExpandedZipFile());

        // read the zip file directly, you get nothing
        assertEquals(0, zip.readIntoString().length());
        OutputStream out = new ByteArrayOutputStream();
        try {
            new BytesCopier(zip.getInputStream(), out).transfer(true);
        } finally {
            out.close();
        }
        assertEquals(0, out.toString().length());

        // read each file
        MongoFile file1 = manifest.getFiles().get(0);
        assertEquals("file1.txt", file1.getFilename());
        assertEquals(LoremIpsum.LOREM_IPSUM, file1.readIntoString());

        MongoFile file2 = manifest.getFiles().get(1);
        assertEquals("file2.txt", file2.getFilename());
        assertEquals(LoremIpsum.LOREM_IPSUM, file2.readIntoString());

        MongoFile file3 = manifest.getFiles().get(2);
        assertEquals("manifest.xml", file3.getFilename());
        assertEquals(XML, file3.readIntoString());

        // read manifest from MongoDB
        MongoManifest manifest2 = store.getManifest(zip.getURL());
        assertTrue(manifest2.getZip().isExpandedZipFile());
        assertEquals(3, manifest2.getFiles().size());

        // read each file, again
        file1 = manifest2.getFiles().get(0);
        assertEquals("file1.txt", file1.getFilename());
        assertEquals(LoremIpsum.LOREM_IPSUM, file1.readIntoString());

        file2 = manifest2.getFiles().get(1);
        assertEquals("file2.txt", file2.getFilename());
        assertEquals(LoremIpsum.LOREM_IPSUM, file2.readIntoString());

        file3 = manifest2.getFiles().get(2);
        assertEquals("manifest.xml", file3.getFilename());
        assertEquals(XML, file3.readIntoString());
    }

    @Test
    public void testFileExtensionToMimeType() {
        FileNameMap mapNew = URLConnection.getFileNameMap();
        assertEquals("application/pdf", mapNew.getContentTypeFor(".pdf"));
        assertEquals("application/zip", mapNew.getContentTypeFor(".zip"));
        assertEquals("image/gif", mapNew.getContentTypeFor(".gif"));
    }

    // Internal
    // ///////////////////
    //
    private static void createFile(final ZipOutputStream out, final String fileName, final byte[] data) throws IOException {
        ZipEntry e = new ZipEntry(fileName);
        out.putNextEntry(e);

        out.write(data, 0, data.length);
        out.closeEntry();
    }

    /* package */static final String XML = "" //
            + " <card xmlns=\"http://businesscard.org\">\n" //
            + "   <name>John Doe</name>\n" //
            + "   <title>CEO, Widget Inc.</title>\n" //
            + "   <email>john.doe@widget.com</email>\n" //
            + "   <phone>(202) 456-1414</phone>\n" //
            + "   <logo url=\"widget.gif\"/>\n"//
            + " </card>";

}
