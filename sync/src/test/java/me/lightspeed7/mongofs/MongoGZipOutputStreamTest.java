package me.lightspeed7.mongofs;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.junit.Test;
import org.mockito.Mockito;

public class MongoGZipOutputStreamTest {

    @Test
    public void test() throws IOException {

        MongoFile mock = Mockito.mock(MongoFile.class);
        Mockito.when(mock.get(MongoFileConstants.length.toString())).thenReturn(Integer.valueOf(100));
        Mockito.when(mock.get(MongoFileConstants.storage.toString())).thenReturn(Integer.valueOf(50));

        ByteArrayOutputStream out = new ByteArrayOutputStream(1024 * 1024);

        OutputStream stream = new MongoGZipOutputStream(mock, out);
        try {
            byte[] bytes = LoremIpsum.LOREM_IPSUM.getBytes();
            stream.write(bytes);
            stream.write(123);

        } finally {
            stream.close();
        }

    }
}
