package me.lightspeed7.mongofs.common;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import me.lightspeed7.mongofs.LoremIpsum;
import me.lightspeed7.mongofs.util.BytesCopier;
import me.lightspeed7.mongofs.writing.BufferedChunksOutputStream;

import org.apache.tools.ant.filters.StringInputStream;
import org.junit.Test;

public class BufferedChunkOutputStreamTest implements LoremIpsum {

    @Test
    public void test8KBuffer256kChunks() throws IOException {

        LogDumpOutputStreamSink log = new LogDumpOutputStreamSink();

        try (BufferedChunksOutputStream stream = new BufferedChunksOutputStream(log)) {
            new BytesCopier(8 * 1024, new StringInputStream(LOREM_IPSUM, "UTF-8"), stream).transfer(false);
        }

        System.out.println(log.info());
        assertEquals(LOREM_IPSUM.length(), log.total);
        assertEquals( //
                "total = 32085, commands = [write(b, 0, 8192), write(b, 0, 8192), write(b, 0, 8192), write(b, 0, 7509), flush, close]", //
                log.info());
    }

    @Test
    public void test16KBuffers5kChunks() throws IOException {

        LogDumpOutputStreamSink log = new LogDumpOutputStreamSink();

        try (BufferedChunksOutputStream stream = new BufferedChunksOutputStream(log, 5 * 1024)) {
            new BytesCopier(16 * 1024, new StringInputStream(LOREM_IPSUM, "UTF-8"), stream).transfer(false);
        }

        System.out.println(log.info());
        assertEquals(LOREM_IPSUM.length(), log.total);
        assertEquals(
                //
                "total = 32085, commands = [write(b, 0, 5120), write(b, 0, 5120), write(b, 0, 5120), write(b, 0, 5120), write(b, 0, 5120), write(b, 0, 5120), write(b, 0, 1365), flush, close]",
                log.info());
    }

    @Test
    public void test13KBuffers15kChunks() throws IOException {

        LogDumpOutputStreamSink log = new LogDumpOutputStreamSink();

        try (BufferedChunksOutputStream stream = new BufferedChunksOutputStream(log, 15 * 1024)) {
            new BytesCopier(13 * 1024, new StringInputStream(LOREM_IPSUM, "UTF-8"), stream).transfer(false);
        }

        System.out.println(log.info());
        assertEquals(LOREM_IPSUM.length(), log.total);
        assertEquals(
        //
                "total = 32085, commands = [write(b, 0, 15360), write(b, 0, 15360), write(b, 0, 1365), flush, close]", log.info());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalChunkSize() throws IOException {

        try (OutputStream out = new BufferedChunksOutputStream(null, -1)) {
            // empty
        }
    }

    @Test
    public void testSingleByteWrite() throws IOException {

        LogDumpOutputStreamSink log = new LogDumpOutputStreamSink();
        try (OutputStream out = new BufferedChunksOutputStream(log, 5)) {
            out.write(Byte.valueOf("1"));
        }

        System.out.println(log.info());

        assertEquals(1, log.total);
        assertEquals(
        //
                "total = 1, commands = [write(b, 0, 1), flush, close]", //
                log.info());
    }

    private class LogDumpOutputStreamSink extends OutputStream {

        List<String> commands = new ArrayList<>();
        long total = 0;

        public String info() {

            return String.format("total = %d, commands = %s", total, commands.toString());
        }

        @Override
        public void write(int b) throws IOException {

            commands.add("write(b)");
            ++total;
        }

        @Override
        public void write(byte[] b) throws IOException {

            commands.add(String.format("write(b) - length = %d", b.length));
            total += b.length;
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {

            commands.add(String.format("write(b, %d, %d)", off, len));
            total += len;
        }

        @Override
        public void flush() throws IOException {

            commands.add("flush");
        }

        @Override
        public void close() throws IOException {

            commands.add("close");
        }

    }

}
