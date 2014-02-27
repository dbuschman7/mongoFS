package me.lightspeed7.mongofs.reading;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import me.lightspeed7.mongofs.LoremIpsum;
import me.lightspeed7.mongofs.MongoFile;
import me.lightspeed7.mongofs.util.BytesCopier;

import org.junit.Test;
import org.mockito.Mockito;

public class CountingInputStreamTest implements LoremIpsum {

    @Test
    public void test()
            throws IOException {

        ByteArrayInputStream lStream = new ByteArrayInputStream(LOREM_IPSUM.getBytes());

        MongoFile mongoFile = Mockito.mock(MongoFile.class);
        Mockito.when(mongoFile.getLength()).thenReturn((long) LOREM_IPSUM.length());

        CountingInputStream in = new CountingInputStream(lStream);

        ByteArrayOutputStream out = new ByteArrayOutputStream(LOREM_IPSUM.length());
        new BytesCopier(in, out).transfer(true);
        // in.close();

        assertEquals(LOREM_IPSUM.length(), out.size());
        assertEquals(LOREM_IPSUM.length(), in.getCount());
        assertEquals("3fb3f7a485c87f6be4c8fb6f4d34c5c8", in.getDigest());

    }

}
