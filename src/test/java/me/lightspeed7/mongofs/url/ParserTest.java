package me.lightspeed7.mongofs.url;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.URL;

import me.lightspeed7.mongofs.MongoFileUrlTest;

import org.bson.types.ObjectId;
import org.junit.Test;

public class ParserTest {

    @Test
    public void testAutoAssignedCompression() throws IOException {

        ObjectId id = new ObjectId();
        URL url = Parser.construct(id, "fileName.pdf", MongoFileUrlTest.PDF, null, true, false);

        assertNotNull(url);
        assertEquals(String.format("mongofile:gz:fileName.pdf?%s#application/pdf", id.toString()), url.toString());

    }

    @Test
    public void testCustomAssignedCompression() throws IOException {

        ObjectId id = new ObjectId();
        URL url = Parser.construct(id, "fileName.pdf", MongoFileUrlTest.PDF, "foo", true, false);

        assertNotNull(url);
        assertEquals(String.format("mongofile:foo:fileName.pdf?%s#application/pdf", id.toString()), url.toString());

    }

    @Test
    public void testNoCompression() throws IOException {

        ObjectId id = new ObjectId();
        URL url = Parser.construct(id, "fileName.zip", MongoFileUrlTest.ZIP, null, true, false);

        assertNotNull(url);
        assertEquals(String.format("mongofile:fileName.zip?%s#application/zip", id.toString()), url.toString());

    }

    @Test
    public void testBlockedCompression() throws IOException {

        ObjectId id = new ObjectId();
        URL url = Parser.construct(id, "fileName.pdf", MongoFileUrlTest.PDF, null, false, false);

        assertNotNull(url);
        assertEquals(String.format("mongofile:fileName.pdf?%s#application/pdf", id.toString()), url.toString());

    }

    @Test
    public void testCustomAssignedOverrideCompression() throws IOException {

        ObjectId id = new ObjectId();
        URL url = Parser.construct(id, "fileName.pdf", MongoFileUrlTest.PDF, "foo", false, false);

        assertNotNull(url);
        assertEquals(String.format("mongofile:foo:fileName.pdf?%s#application/pdf", id.toString()), url.toString());

    }

    @Test
    public void testAssignedEncryption() throws IOException {

        ObjectId id = new ObjectId();
        URL url = Parser.construct(id, "fileName.pdf", MongoFileUrlTest.PDF, null, false, true);

        assertNotNull(url);
        assertEquals(String.format("mongofile:enc:fileName.pdf?%s#application/pdf", id.toString()), url.toString());

    }
}
