package me.lightspeed7.mongofs.spring;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.UnknownHostException;

import me.lightspeed7.mongofs.LoremIpsum;
import me.lightspeed7.mongofs.MongoFile;
import me.lightspeed7.mongofs.MongoFileConstants;
import me.lightspeed7.mongofs.MongoFileReader;
import me.lightspeed7.mongofs.MongoFileStore;
import me.lightspeed7.mongofs.MongoFileWriter;
import me.lightspeed7.mongofs.url.MongoFileUrl;
import me.lightspeed7.mongofs.util.BytesCopier;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.FileSystemXmlApplicationContext;

public class SpringStandUpTest {

    @BeforeClass
    public static void setup() throws UnknownHostException {

        SpringContext.setCtx(new FileSystemXmlApplicationContext("src/test/resources/spring.xml"));
        SpringContext.dumpDefinedBeanNames();

    }

    @Test
    public void testSpringJavaConfig() throws IOException {

        MongoFileStore store = SpringContext.getBean("mongoFileStore");

        doRoundTrip(store, "file1.txt");
    }

    @Test
    public void testSpringXMLConfig() throws IOException {

        MongoFileStore store = SpringContext.getBean("mongoFileStore2");

        doRoundTrip(store, "file2.txt");
    }

    private void doRoundTrip(final MongoFileStore store, final String filename) throws IOException {

        MongoFileWriter writer = store.createNew(filename, "text/plain", null, true);
        ByteArrayInputStream in = new ByteArrayInputStream(LoremIpsum.LOREM_IPSUM.getBytes());
        OutputStream out = writer.getOutputStream();
        try {
            new BytesCopier(in, out).transfer(true);
        } finally {
            out.close();
        }

        // verify it exists
        MongoFile mongoFile = writer.getMongoFile();
        assertTrue(store.exists(mongoFile.getURL()));

        // read a file
        assertEquals(true, mongoFile.getURL().isStoredCompressed());
        assertEquals(LoremIpsum.LOREM_IPSUM.length(), mongoFile.getLength());

        assertNotNull(mongoFile.get(MongoFileConstants.storageLength)); // verify compression
        assertEquals(MongoFileUrl.GZIPPED, mongoFile.get(MongoFileConstants.compressionFormat)); // verify compression
        assertNotNull(mongoFile.get(MongoFileConstants.compressionRatio)); // verify compression

        ByteArrayOutputStream out2 = new ByteArrayOutputStream(32 * 1024);
        new BytesCopier(new MongoFileReader(store, mongoFile).getInputStream(), out2).transfer(true);
        assertEquals(LoremIpsum.LOREM_IPSUM, out2.toString());
    }

}
