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

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

public class MongoFileCursorTest implements LoremIpsum {

    private static final String DB_NAME = "MongoFSTest-cursor";
    private static final String BUCKET = "cursor";

    private static DB database;

    private static MongoClient mongoClient;
    private static MongoFileStore store;

    // initializer
    @BeforeClass
    public static void initial() throws IllegalArgumentException, IOException {

        mongoClient = MongoTestConfig.construct();

        mongoClient.dropDatabase(DB_NAME);
        database = mongoClient.getDB(DB_NAME);

        store = new MongoFileStore(database, new MongoFileStoreConfig(BUCKET));

        createFile(store, "/foo/bar1.txt", "text/plain");
        createFile(store, "/foo/bar4.txt", "text/plain");
        createFile(store, "/baz/bar3.txt", "text/plain");
        createFile(store, "/foo/bar1.txt", "text/plain");
    }

    @Test
    public void testSimpleList() throws IllegalArgumentException, IOException {

        MongoFileQuery query = store.query();
        MongoFileCursor fileList = query.getFileList();
        int count = 0;
        for (MongoFile mongoFile : fileList) {
            ++count;
            assertNotNull(mongoFile.getURL());
        }
        assertEquals(4, count);
    }

    @Test
    public void testFilterFileNameList() throws IllegalArgumentException, IOException {

        ByteArrayOutputStream out = new ByteArrayOutputStream(32 * 1024);

        MongoFileCursor cursor = store.query().find("/foo/bar1.txt");
        int count = 0;
        for (MongoFile mongoFile : cursor) {
            ++count;
            assertNotNull(mongoFile.getURL());
            assertEquals("/foo/bar1.txt", mongoFile.getFilename());
            InputStream in = store.read(mongoFile);
            new BytesCopier(in, out).transfer(true); // append more than one file together
        }
        assertEquals(2, count);
        assertEquals(LOREM_IPSUM.length() * 2, out.toString().length());
    }

    @Test
    public void testSortedList() throws IllegalArgumentException, IOException {

        MongoFileCursor fileList = store.query().getFileList().sort(BasicDBObjectBuilder.start("filename", "1").get());

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
    public void testSortedFilteredList() throws IllegalArgumentException, IOException {

        store.getFilesCollection().ensureIndex(BasicDBObjectBuilder.start("md5", 1).get());
        DBObject q = BasicDBObjectBuilder.start("filename", "/foo/bar1.txt").get();
        DBObject s = BasicDBObjectBuilder.start("filename", "1").get();

        MongoFileCursor fileList = store.query().getFileList(q, s);

        assertTrue(fileList.hasNext());
        assertEquals("/foo/bar1.txt", fileList.next().getFilename());

        assertTrue(fileList.hasNext());
        assertEquals("/foo/bar1.txt", fileList.next().getFilename());

        assertFalse(fileList.hasNext());
    }

    @Test
    public void testFindList() throws IllegalArgumentException, IOException {

        MongoFileQuery query = new MongoFileQuery(store);
        List<MongoFile> fileList = query.find("/foo/bar1.txt").toList();

        assertEquals(2, fileList.size());
        assertEquals("/foo/bar1.txt", fileList.get(0).getFilename());
        assertEquals("/foo/bar1.txt", fileList.get(1).getFilename());
    }

    //
    // internal
    // //////////////////
    private static void createFile(MongoFileStore store, String filename, String mediaType) throws IOException {

        MongoFileWriter writer = store.createNew(filename, mediaType, null, true);
        writer.write(new ByteArrayInputStream(LOREM_IPSUM.getBytes()));
    }
}
