package me.lightspeed7.mongofs.writing;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import me.lightspeed7.mongofs.LoremIpsum;
import me.lightspeed7.mongofs.MongoFile;
import me.lightspeed7.mongofs.common.MongoFileConstants;

import org.junit.Test;
import org.mockito.Mockito;

public class CountingOutputStreamTest implements LoremIpsum {

    @Test
    public void test() throws IOException {

        MongoFile mock = Mockito.mock(MongoFile.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream(1024 * 1024);

        try (CountingOutputStream stream = new CountingOutputStream(MongoFileConstants.chunkSize, mock, out)) {
            byte[] bytes = LOREM_IPSUM.getBytes();
            stream.write(bytes);
            stream.write(123);

            assertTrue(stream.count == bytes.length + 1);
        }

    }
}
