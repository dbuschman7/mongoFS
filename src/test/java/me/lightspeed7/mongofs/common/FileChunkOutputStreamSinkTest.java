package me.lightspeed7.mongofs.common;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import me.lightspeed7.mongofs.common.FileChunksOutputStreamSink;
import me.lightspeed7.mongofs.gridfs.GridFSInputFile;
import me.lightspeed7.mongofs.gridfs.GridFSInputFileAdapter;

import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.mongodb.DB;
import com.mongodb.DummyCollection;

public class FileChunkOutputStreamSinkTest {

    private DummyCollection collection;
    private ObjectId id;
    private GridFSInputFileAdapter adapter;

    @Before
    public void before() {

        DB mockDB = Mockito.mock(DB.class);
        Mockito.when(mockDB.getName()).thenReturn("foobar");

        collection = new DummyCollection(mockDB, "foo");
        id = new ObjectId();
        adapter = new GridFSInputFileAdapter(Mockito.mock(GridFSInputFile.class));
    }

    @Test
    public void testFullBufferWrite()
            throws IOException {

        try (FileChunksOutputStreamSink sink = new FileChunksOutputStreamSink(collection, id, adapter)) {
            byte[] array = "This is a test".getBytes();
            sink.write(array, 0, array.length);
        }

        // assert
        assertEquals(//
                String.format("{ \"files_id\" : { \"$oid\" : \"%s\"} , \"n\" : 0 , \"data\" : <Binary Data>}",//
                        id.toString()//
                ),//
                collection.list.get(0).toString());

        byte[] bytes = (byte[]) collection.list.get(0).get("data");
        assertEquals(14, bytes.length);
        assertEquals("This is a test", new String(bytes, "UTF-8"));

    }

    @Test
    public void testPartialBufferWrite()
            throws IOException {

        try (FileChunksOutputStreamSink sink = new FileChunksOutputStreamSink(collection, id, adapter)) {
            byte[] array = "This is a test".getBytes();
            sink.write(array, 10, 4);
        }

        // assert
        assertEquals(//
                String.format("{ \"files_id\" : { \"$oid\" : \"%s\"} , \"n\" : 0 , \"data\" : <Binary Data>}",//
                        id.toString()//
                ),//
                collection.list.get(0).toString());

        byte[] bytes = (byte[]) collection.list.get(0).get("data");
        assertEquals(4, bytes.length);

        assertEquals("test", new String(bytes, "UTF-8"));
    }
}
