package me.lightspeed7.mongofs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import me.lightspeed7.mongofs.util.BytesCopier;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mongodb.Document;

import com.mongodb.MongoClient;

public class MongoFileCursorTest {

    private static final String DB_NAME = "MongoFSTest-cursor";
    private static final String BUCKET = "cursor";

    private static MongoFileStore store;

    // initializer
    @BeforeClass
    public static void initial() throws IOException {

        MongoClient mongoClient = MongoTestConfig.construct();

        mongoClient.dropDatabase(DB_NAME);

        store = new MongoFileStore(mongoClient.getDB(DB_NAME), MongoFileStoreConfig.builder().bucket(BUCKET).build());

        createFile(store, "/foo/bar1.txt", "text/plain");
        createFile(store, "/foo/bar4.txt", "text/plain");
        createFile(store, "/baz/bar3.txt", "text/plain");
        createFile(store, "/foo/bar1.txt", "text/plain");
    }

    @Test
    public void testFilterFileNameList() throws IOException {

        ByteArrayOutputStream out = new ByteArrayOutputStream(32 * 1024);

        MongoFileCursor cursor = store.find("/foo/bar1.txt");
        int count = 0;
        for (MongoFile mongoFile : cursor) {
            ++count;
            assertNotNull(mongoFile.getURL());
            assertEquals("/foo/bar1.txt", mongoFile.getFilename());
            InputStream in = new MongoFileReader(store, mongoFile).getInputStream();
            new BytesCopier(in, out).transfer(true); // append more than one file together
        }
        assertEquals(2, count);
        assertEquals(LoremIpsum.LOREM_IPSUM.length() * 2, out.toString().length());
    }

    @Test
    public void testSortedList() throws IOException {

        MongoFileCursor fileList = store.find(new Document("contentType", "text/plain"), new Document("filename", 1));

        assertTrue(fileList.hasNext());
        assertEquals("/baz/bar3.txt", fileList.next().getFilename());

        assertTrue(fileList.hasNext());
        assertEquals("/foo/bar1.txt", fileList.next().getFilename());

        assertTrue(fileList.hasNext());
        assertEquals("/foo/bar1.txt", fileList.next().getFilename());

        assertTrue(fileList.hasNext());
        assertEquals("/foo/bar4.txt", fileList.next().getFilename());

        assertFalse(fileList.hasNext());
    }

    @Test
    public void testSortedFilteredList() throws IOException {

        store.getFilesCollection().createIndex(new Document("md5", 1));

        MongoFileCursor fileList = store.find(new Document("filename", "/foo/bar1.txt"), new Document("filename", 1));

        assertTrue(fileList.hasNext());
        assertEquals("/foo/bar1.txt", fileList.next().getFilename());

        assertTrue(fileList.hasNext());
        assertEquals("/foo/bar1.txt", fileList.next().getFilename());

        assertFalse(fileList.hasNext());
    }

    @Test
    public void testFindList() throws IOException {

        List<MongoFile> fileList = store.find("/foo/bar1.txt").toList();

        assertEquals(2, fileList.size());
        assertEquals("/foo/bar1.txt", fileList.get(0).getFilename());
        assertEquals("/foo/bar1.txt", fileList.get(1).getFilename());
    }

    //
    // internal
    // //////////////////
    private static void createFile(final MongoFileStore store, final String filename, final String mediaType) throws IOException {

        MongoFileWriter writer = store.createNew(filename, mediaType, null, true);
        writer.write(new ByteArrayInputStream(LoremIpsum.LOREM_IPSUM.getBytes()));
    }
}
