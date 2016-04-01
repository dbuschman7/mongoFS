package me.lightspeed7.mongofs;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import me.lightspeed7.mongofs.crypto.BasicCrypto;
import me.lightspeed7.mongofs.util.BytesCopier;
import me.lightspeed7.mongofs.util.ChunkSize;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EncryptChunkOutputStreamTest {

    public static final Logger LOGGER = LoggerFactory.getLogger("file");

    @Test
    public void test16KBuffers8kChunks() throws IOException {

        LogDumpOutputStreamSink log = new LogDumpOutputStreamSink();

        BasicCrypto crypto = new BasicCrypto(ChunkSize.tiny_8K);
        EncryptChunkOutputStream stream = new EncryptChunkOutputStream(crypto, log);
        BufferedChunksOutputStream chunks = new BufferedChunksOutputStream(stream, crypto.getChunkSize());
        try {
            new BytesCopier(16 * 1024, new ByteArrayInputStream(LoremIpsum.getBytes()), chunks).transfer(true);
        } finally {
            stream.close();
        }

        LOGGER.debug("LoremIpsum length = " + LoremIpsum.getString().length());
        // System.out.println(log.info());
        assertEquals(32168, log.total);
        assertEquals(
        //
                "total = 32168, commands = [" //
                        + "write(b), write(b), write(b), write(b), write(b), write(b), write(b), write(b), write(b, 0, 7488), " //
                        + "write(b), write(b), write(b), write(b), write(b), write(b), write(b), write(b), write(b, 0, 7488), " //
                        + "write(b), write(b), write(b), write(b), write(b), write(b), write(b), write(b), write(b, 0, 7488), " //
                        + "write(b), write(b), write(b), write(b), write(b), write(b), write(b), write(b), write(b, 0, 7488), " //
                        + "write(b), write(b), write(b), write(b), write(b), write(b), write(b), write(b), write(b, 0, 2176), " //
                        + "flush, close]", log.info());
    }

    private class LogDumpOutputStreamSink extends OutputStream {

        private List<String> commands = new ArrayList<String>();
        private long total = 0;

        public String info() {

            return String.format("total = %d, commands = %s", total, commands.toString());
        }

        @Override
        public void write(final int b) throws IOException {

            commands.add("write(b)");
            ++total;
        }

        @Override
        public void write(final byte[] b) throws IOException {

            commands.add(String.format("write(b) - length = %d", b.length));
            total += b.length;
        }

        @Override
        public void write(final byte[] b, final int off, final int len) throws IOException {

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
