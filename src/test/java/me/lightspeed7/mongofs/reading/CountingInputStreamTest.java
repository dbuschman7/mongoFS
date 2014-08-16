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

public class CountingInputStreamTest {

    @Test
    public void test() throws IOException {

        ByteArrayInputStream lStream = new ByteArrayInputStream(LoremIpsum.LOREM_IPSUM.getBytes());

        MongoFile mongoFile = Mockito.mock(MongoFile.class);
        Mockito.when(mongoFile.getLength()).thenReturn((long) LoremIpsum.LOREM_IPSUM.length());

        CountingInputStream in = new CountingInputStream(mongoFile, lStream);

        ByteArrayOutputStream out = new ByteArrayOutputStream(LoremIpsum.LOREM_IPSUM.length());
        new BytesCopier(in, out).transfer(true);
        // in.close();

        assertEquals(LoremIpsum.LOREM_IPSUM.length(), out.size());
        assertEquals(LoremIpsum.LOREM_IPSUM.length(), in.getCount());

    }

}
