package me.lightspeed7.mongofs.zip;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.regex.Pattern;

import me.lightspeed7.mongofs.MongoFile;
import me.lightspeed7.mongofs.MongoFileConstants;
import me.lightspeed7.mongofs.MongoFileCursor;
import me.lightspeed7.mongofs.MongoFileStore;
import me.lightspeed7.mongofs.MongoFileStoreConfig;
import me.lightspeed7.mongofs.MongoFileWriter;
import me.lightspeed7.mongofs.MongoManifest;
import me.lightspeed7.mongofs.MongoTestConfig;
import me.lightspeed7.mongofs.MongoZipArchiveQuery;
import me.lightspeed7.mongofs.util.FileUtil;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mongodb.Document;
import org.mongodb.MongoDatabase;

import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;

public class MongoManifestQueryTest {

    static final String DB_NAME = "MongoFSTest-zipExpander";

    private static MongoManifest manifest;

    private static MongoFileStore store;

    // initializer
    @BeforeClass
    public static void initial() throws IOException {

        MongoClient mongoClient = MongoTestConfig.construct();

        mongoClient.dropDatabase(DB_NAME);
        MongoDatabase database = new MongoDatabase(mongoClient.getDB(DB_NAME));

        File file = ZipFileExpanderTest.generateZipFile();

        MongoFileStoreConfig config = MongoFileStoreConfig.builder()//
                .bucket("manifestQuery")//
                .chunkSize(MongoFileStoreConfig.DEFAULT_CHUNKSIZE)//
                .enableCompression(true)//
                .writeConcern(WriteConcern.SAFE) //
                .build();

        store = new MongoFileStore(database, config);
        String filename = file.getCanonicalPath();
        MongoFileWriter writer = store.createNew(filename, FileUtil.getContentType(filename));

        manifest = writer.uploadZipFile(new FileInputStream(file));

    }

    @Test
    public void testSearchManifestReturnOne() throws Exception {

        MongoZipArchiveQuery zipArchiveQuery = store.findInZipArchive(manifest.getZip());
        assertNotNull(zipArchiveQuery);

        MongoFileCursor find = zipArchiveQuery.find("file1.txt");
        assertNotNull(find);
        assertTrue(find.hasNext());
        MongoFile mongoFile = find.next();
        assertNotNull(mongoFile);
        assertEquals("file1.txt", mongoFile.getFilename());
        assertFalse(find.hasNext()); // should only be one returned

    }

    @Test
    public void testSearchZipArchiveRegex() throws Exception {

        MongoZipArchiveQuery zipArchiveQuery = store.findInZipArchive(manifest.getZip());
        assertNotNull(zipArchiveQuery);

        Pattern namesRegex = Pattern.compile("^file.*");
        Document query = new Document(MongoFileConstants.filename.name(), namesRegex);

        MongoFileCursor find = zipArchiveQuery.find(query);
        assertNotNull(find);
        assertTrue(find.hasNext());
        MongoFile mongoFile = find.next();
        assertNotNull(mongoFile);
        assertEquals("file1.txt", mongoFile.getFilename());
        assertTrue(find.hasNext());
        mongoFile = find.next();
        assertNotNull(mongoFile);
        assertEquals("file2.txt", mongoFile.getFilename());
        assertFalse(find.hasNext()); // should only be two returned
    }

    @Test
    public void testSearchZipArchiveRegexSorted() throws Exception {

        MongoZipArchiveQuery zipArchiveQuery = store.findInZipArchive(manifest.getZip());
        assertNotNull(zipArchiveQuery);

        Pattern namesRegex = Pattern.compile("^file.*");
        Document query = new Document(MongoFileConstants.filename.name(), namesRegex);
        Document sort = new Document(MongoFileConstants.filename.name(), -1);

        MongoFileCursor find = zipArchiveQuery.find(query, sort);
        assertNotNull(find);
        assertTrue(find.hasNext());
        MongoFile mongoFile = find.next();
        assertNotNull(mongoFile);
        assertEquals("file2.txt", mongoFile.getFilename());
        assertTrue(find.hasNext());
        mongoFile = find.next();
        assertNotNull(mongoFile);
        assertEquals("file1.txt", mongoFile.getFilename());
        assertFalse(find.hasNext()); // should only be two returned
    }
}
