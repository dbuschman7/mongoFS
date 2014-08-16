package sun.net.www.protocol.mongofile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.URL;

import org.bson.types.ObjectId;
import org.junit.Test;

public class ParserTest {

    @Test
    public void testAutoAssignedCompression() throws IOException {

        ObjectId id = new ObjectId();
        URL url = Parser.construct(id, "fileName.pdf", "application/pdf", null, true);

        assertNotNull(url);
        assertEquals(String.format("mongofile:gz:fileName.pdf?%s#application/pdf", id.toString()), url.toString());

    }

    @Test
    public void testCustomAssignedCompression() throws IOException {

        ObjectId id = new ObjectId();
        URL url = Parser.construct(id, "fileName.pdf", "application/pdf", "foo", true);

        assertNotNull(url);
        assertEquals(String.format("mongofile:foo:fileName.pdf?%s#application/pdf", id.toString()), url.toString());

    }

    @Test
    public void testNoCompression() throws IOException {

        ObjectId id = new ObjectId();
        URL url = Parser.construct(id, "fileName.zip", "application/zip", null, true);

        assertNotNull(url);
        assertEquals(String.format("mongofile:fileName.zip?%s#application/zip", id.toString()), url.toString());

    }

    @Test
    public void testBlockedCompression() throws IOException {

        ObjectId id = new ObjectId();
        URL url = Parser.construct(id, "fileName.pdf", "application/pdf", null, false);

        assertNotNull(url);
        assertEquals(String.format("mongofile:fileName.pdf?%s#application/pdf", id.toString()), url.toString());

    }

    @Test
    public void testCustomAssignedOverrideCompression() throws IOException {

        ObjectId id = new ObjectId();
        URL url = Parser.construct(id, "fileName.pdf", "application/pdf", "foo", false);

        assertNotNull(url);
        assertEquals(String.format("mongofile:foo:fileName.pdf?%s#application/pdf", id.toString()), url.toString());

    }
}
