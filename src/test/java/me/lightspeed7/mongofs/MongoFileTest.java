package me.lightspeed7.mongofs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;

import java.math.BigInteger;

import org.bson.types.ObjectId;
import org.junit.Test;
import org.mockito.Mockito;
import org.mongodb.CommandResult;
import org.mongodb.Document;
import org.mongodb.MongoCollection;
import org.mongodb.MongoDatabase;
import org.mongodb.MongoException;

public class MongoFileTest {

    @Test
    public void testGetBoolean() {

        Document surrogate = new Document()//
                .append(MongoFileConstants.chunkCount.name(), Integer.valueOf(1))//
                .append(MongoFileConstants.ratio.name(), BigInteger.ZERO)//
                .append(MongoFileConstants.deleted.name(), Boolean.FALSE);
        MongoFile mongoFile = new MongoFile(null, surrogate);

        assertTrue(mongoFile.getBoolean(MongoFileConstants.chunkCount, false));
        assertFalse(mongoFile.getBoolean(MongoFileConstants.ratio, true));
        assertFalse(mongoFile.getBoolean(MongoFileConstants.deleted, true));
        assertFalse(mongoFile.getBoolean(MongoFileConstants.expireAt, false));

    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetBooleanInvalid() {
        Document surrogate = new Document()//
                .append(MongoFileConstants.chunkCount.name(), "This is a string");

        MongoFile mongoFile = new MongoFile(null, surrogate);
        assertTrue(mongoFile.getBoolean(MongoFileConstants.chunkCount, false));

    }

    @Test
    public void testGetters() {
        Document surrogate = new Document()//
                .append(MongoFileConstants.compressedLength.name(), 123L);

        MongoFile mongoFile = new MongoFile(null, surrogate);
        assertEquals(123, mongoFile.getStorageLength());

        assertEquals(-1, mongoFile.getInt(MongoFileConstants.chunkSize));
        assertEquals(234, mongoFile.getLong(MongoFileConstants.chunkSize, 234));
        assertEquals(1234.56, mongoFile.getDouble(MongoFileConstants.chunkSize, 1234.56), 0.01);
        assertEquals("def", mongoFile.getString(MongoFileConstants.chunkSize, "def"));

        ObjectId objectId = new ObjectId();
        assertEquals(objectId, mongoFile.getObjectId(MongoFileConstants._id, objectId));

    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetStringKeyNull() {
        Document surrogate = new Document();

        MongoFile mongoFile = new MongoFile(null, surrogate);
        mongoFile.getString(null, "def");

    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetDoubleKeyNull() {
        Document surrogate = new Document();

        MongoFile mongoFile = new MongoFile(null, surrogate);
        mongoFile.getDouble(null, 0.0);

    }

    @Test(expected = MongoException.class)
    public void noMd5() {
        Document surrogate = new Document();

        MongoFile mongoFile = new MongoFile(null, surrogate);
        mongoFile.validate();
    }

    @Test(expected = MongoException.class)
    public void md5Differ() {

        // setup
        MongoFileStore store = Mockito.mock(MongoFileStore.class);

        @SuppressWarnings("unchecked")
        MongoCollection<Document> coll = (MongoCollection<Document>) Mockito.mock(MongoCollection.class);
        Mockito.when(store.getFilesCollection()).thenReturn(coll);

        MongoDatabase database = Mockito.mock(MongoDatabase.class);
        Mockito.when(coll.getDatabase()).thenReturn(database);
        Mockito.when(coll.getName()).thenReturn("bucket");

        CommandResult result = Mockito.mock(CommandResult.class);
        Mockito.when(database.executeCommand(any(Document.class))).thenReturn(result);

        Document server = new Document(MongoFileConstants.md5.name(), "12345");
        Mockito.when(result.getResponse()).thenReturn(server);

        // execute

        Document surrogate = new Document(MongoFileConstants.md5.name(), "54321");

        MongoFile mongoFile = new MongoFile(store, surrogate);
        mongoFile.validate();
    }

    @Test(expected = MongoException.class)
    public void noMd5OnServer() {

        // setup
        MongoFileStore store = Mockito.mock(MongoFileStore.class);

        @SuppressWarnings("unchecked")
        MongoCollection<Document> coll = (MongoCollection<Document>) Mockito.mock(MongoCollection.class);
        Mockito.when(store.getFilesCollection()).thenReturn(coll);

        MongoDatabase database = Mockito.mock(MongoDatabase.class);
        Mockito.when(coll.getDatabase()).thenReturn(database);
        Mockito.when(coll.getName()).thenReturn("bucket");

        CommandResult result = Mockito.mock(CommandResult.class);
        Mockito.when(database.executeCommand(any(Document.class))).thenReturn(result);

        Mockito.when(result.getResponse()).thenReturn(null);

        // execute

        Document surrogate = new Document(MongoFileConstants.md5.name(), "54321");

        MongoFile mongoFile = new MongoFile(store, surrogate);
        mongoFile.validate();
    }

}
