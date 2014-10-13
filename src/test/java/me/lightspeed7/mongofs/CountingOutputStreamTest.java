package me.lightspeed7.mongofs;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import me.lightspeed7.mongofs.CountingOutputStream;
import me.lightspeed7.mongofs.MongoFile;
import me.lightspeed7.mongofs.MongoFileConstants;

import org.junit.Test;
import org.mockito.Mockito;

public class CountingOutputStreamTest {

    @Test
    public void test() throws IOException {

        MongoFile mock = Mockito.mock(MongoFile.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream(1024 * 1024);

        CountingOutputStream stream = new CountingOutputStream(MongoFileConstants.chunkSize, mock, out);
        try {
            byte[] bytes = LoremIpsum.LOREM_IPSUM.getBytes();
            stream.write(bytes);
            stream.write(123);

            assertTrue(stream.getCount() == bytes.length + 1);
        } finally {
            stream.close();
        }

    }
}
