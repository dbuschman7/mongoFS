package sun.net.www.protocol.mongofile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.URL;

import org.junit.Test;

public class ParserTest {

    @Test
    public void testAutoAssignedCompression()
            throws IOException {

        URL url = Parser.construct("id", "fileName.pdf", "application/pdf", null);

        assertNotNull(url);
        assertEquals("mongofile:gz:fileName.pdf?id#application/pdf", url.toString());

    }

    @Test
    public void testCustomAssignedCompression()
            throws IOException {

        URL url = Parser.construct("id", "fileName.pdf", "application/pdf", "foo");

        assertNotNull(url);
        assertEquals("mongofile:foo:fileName.pdf?id#application/pdf", url.toString());

    }

    @Test
    public void testNoCompression()
            throws IOException {

        URL url = Parser.construct("id", "fileName.zip", "application/zip", null);

        assertNotNull(url);
        assertEquals("mongofile:fileName.zip?id#application/zip", url.toString());

    }
}
