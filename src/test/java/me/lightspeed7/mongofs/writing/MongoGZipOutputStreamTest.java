package me.lightspeed7.mongofs.writing;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import me.lightspeed7.mongofs.LoremIpsum;
import me.lightspeed7.mongofs.MongoFile;
import me.lightspeed7.mongofs.common.MongoFileConstants;

import org.junit.Test;
import org.mockito.Mockito;

public class MongoGZipOutputStreamTest implements LoremIpsum {

    @Test
    public void test() throws IOException {

        MongoFile mock = Mockito.mock(MongoFile.class);
        Mockito.when(mock.get(MongoFileConstants.length.toString())).thenReturn(Integer.valueOf(100));
        Mockito.when(mock.get(MongoFileConstants.compressedLength.toString())).thenReturn(Integer.valueOf(50));

        ByteArrayOutputStream out = new ByteArrayOutputStream(1024 * 1024);

        try (OutputStream stream = new MongoGZipOutputStream(mock, out)) {
            byte[] bytes = LOREM_IPSUM.getBytes();
            stream.write(bytes);
            stream.write(123);

        }

    }
}
