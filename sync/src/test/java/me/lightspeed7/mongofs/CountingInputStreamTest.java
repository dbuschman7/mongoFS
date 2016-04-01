package me.lightspeed7.mongofs;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

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

    @Test
    public void testMarkSkip() throws IOException {

        ByteArrayInputStream lStream = new ByteArrayInputStream(LoremIpsum.LOREM_IPSUM.getBytes());

        MongoFile mongoFile = Mockito.mock(MongoFile.class);
        Mockito.when(mongoFile.getLength()).thenReturn((long) LoremIpsum.LOREM_IPSUM.length());

        CountingInputStream in = new CountingInputStream(mongoFile, lStream);

        in.skip(100);
        in.mark(100);

    }

    @Test(expected = IOException.class)
    public void testMarkNotSupported() throws IOException {

        ByteArrayInputStream lStream = new ByteArrayInputStream(LoremIpsum.LOREM_IPSUM.getBytes());

        MongoFile mongoFile = Mockito.mock(MongoFile.class);
        Mockito.when(mongoFile.getLength()).thenReturn((long) LoremIpsum.LOREM_IPSUM.length());
        Mockito.when(mongoFile.getLong(MongoFileConstants.storage)).thenReturn((long) LoremIpsum.LOREM_IPSUM.length());
        Mockito.when(mongoFile.containsKey(MongoFileConstants.storage.name())).thenReturn(true);

        CountingInputStream in = new CountingInputStream(mongoFile, new NotSupportedInputStream(lStream));
        try {
            in.reset();
        } finally {
            in.close();
        }

    }

    @Test(expected = IOException.class)
    public void testMarkSupportedButNotSet() throws IOException {

        ByteArrayInputStream lStream = new ByteArrayInputStream(LoremIpsum.LOREM_IPSUM.getBytes());

        MongoFile mongoFile = Mockito.mock(MongoFile.class);
        Mockito.when(mongoFile.getLength()).thenReturn((long) LoremIpsum.LOREM_IPSUM.length());
        Mockito.when(mongoFile.getLong(MongoFileConstants.compressedLength)).thenReturn((long) LoremIpsum.LOREM_IPSUM.length());
        Mockito.when(mongoFile.containsKey(MongoFileConstants.compressedLength.name())).thenReturn(true);

        CountingInputStream in = new CountingInputStream(mongoFile, new SupportedInputStream(lStream));
        try {
            in.reset();
        } finally {
            in.close();
        }

    }

    @Test(expected = IOException.class)
    public void testExpectedNotEqualLength() throws IOException {

        ByteArrayInputStream lStream = new ByteArrayInputStream(LoremIpsum.LOREM_IPSUM.getBytes());

        MongoFile mongoFile = Mockito.mock(MongoFile.class);
        Mockito.when(mongoFile.getLength()).thenReturn((long) LoremIpsum.LOREM_IPSUM.length());
        Mockito.when(mongoFile.getLong(MongoFileConstants.storage)).thenReturn(0L);
        Mockito.when(mongoFile.containsKey(MongoFileConstants.storage.name())).thenReturn(true);

        new CountingInputStream(mongoFile, new SupportedInputStream(lStream)).close();

    }

    @Test
    public void testClose() throws IOException {

        ByteArrayInputStream lStream = new ByteArrayInputStream(LoremIpsum.LOREM_IPSUM.getBytes());

        MongoFile mongoFile = Mockito.mock(MongoFile.class);
        Mockito.when(mongoFile.isCompressed()).thenReturn(true);
        Mockito.when(mongoFile.getLong(MongoFileConstants.storage)).thenReturn(0L);
        Mockito.when(mongoFile.containsKey(MongoFileConstants.storage.name())).thenReturn(true);

        CountingInputStream in = new CountingInputStream(mongoFile, lStream);

        in.close();
    }

    @Test(expected = IOException.class)
    public void testClose2() throws IOException {

        ByteArrayInputStream lStream = new ByteArrayInputStream(LoremIpsum.LOREM_IPSUM.getBytes());

        MongoFile mongoFile = Mockito.mock(MongoFile.class);
        Mockito.when(mongoFile.isCompressed()).thenReturn(true);
        Mockito.when(mongoFile.getLong(MongoFileConstants.compressedLength)).thenReturn((long) LoremIpsum.LOREM_IPSUM.length());
        Mockito.when(mongoFile.containsKey(MongoFileConstants.compressedLength.name())).thenReturn(true);

        CountingInputStream in = new CountingInputStream(mongoFile, lStream);

        in.close();
    }

    @Test(expected = IllegalStateException.class)
    public void testClose3() throws IOException {

        ByteArrayInputStream lStream = new ByteArrayInputStream(LoremIpsum.LOREM_IPSUM.getBytes());

        MongoFile mongoFile = Mockito.mock(MongoFile.class);
        Mockito.when(mongoFile.isCompressed()).thenReturn(true);

        CountingInputStream in = new CountingInputStream(mongoFile, lStream);

        in.close();
    }

    @Test
    public void testReset() throws IOException {

        ByteArrayInputStream lStream = new ByteArrayInputStream(LoremIpsum.LOREM_IPSUM.getBytes());

        MongoFile mongoFile = Mockito.mock(MongoFile.class);
        Mockito.when(mongoFile.isCompressed()).thenReturn(true);

        CountingInputStream in = new CountingInputStream(mongoFile, lStream);

        int read = in.read();
        in.mark(100);
        in.reset();
    }

    //
    //
    // Mark not supported
    class NotSupportedInputStream extends FilterInputStream {

        protected NotSupportedInputStream(final InputStream in) {
            super(in);
        }

        @Override
        public boolean markSupported() {
            return false;
        }

    }

    class SupportedInputStream extends FilterInputStream {

        protected SupportedInputStream(final InputStream in) {
            super(in);
        }

        @Override
        public boolean markSupported() {
            return true;
        }

    }
}
