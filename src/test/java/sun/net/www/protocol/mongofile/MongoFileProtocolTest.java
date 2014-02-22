package sun.net.www.protocol.mongofile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import me.lightspeed7.mongofs.MongoFile;

import org.junit.Test;

import com.google.common.net.MediaType;

public class MongoFileProtocolTest {

    @Test
    public void testKnown() {

        MongoFileURLStreamHandlerFactory factory = new MongoFileURLStreamHandlerFactory();
        URLStreamHandler x = factory.createURLStreamHandler(MongoFile.PROTOCOL);
        assertNotNull(x);
        assertEquals(x.getClass().getName(), sun.net.www.protocol.mongofile.Handler.class.getName());
    }

    @Test
    public void testUnknown() {

        MongoFileURLStreamHandlerFactory factory = new MongoFileURLStreamHandlerFactory();
        URLStreamHandler x = factory.createURLStreamHandler("foobar");
        assertNull(x);
    }

    @Test( expected = UnsupportedOperationException.class )
    public void testHandlerOpenConnection()
            throws IOException {

        MongoFile url = MongoFile.construct("id", "fileName.pdf", MediaType.PDF.toString());
        URLConnection connection = url.getUrl().openConnection();

        fail("This test should throw an exception");
        assert connection != null; // dummy line to get rid of unused warning
    }

    @Test( expected = UnsupportedOperationException.class )
    public void testHandlerGetContent()
            throws IOException {

        MongoFile url = MongoFile.construct("id", "fileName.pdf", MediaType.PDF.toString());
        Object object = url.getUrl().getContent();

        fail("This test should throw an exception");
        assert object != null; // dummy line to get rid of unused warning
    }

}
