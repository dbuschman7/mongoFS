package me.lightspeed7.mongofs.reading;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import me.lightspeed7.mongofs.MongoFile;
import me.lightspeed7.mongofs.MongoFileConstants;

public class CountingInputStream extends FilterInputStream {

    private long count = 0;
    private long mark = -1;

    private MongoFile file;

    public CountingInputStream(final MongoFile file, final InputStream given) {

        super(given);
        this.file = file;

    }

    @Override
    public int read() throws IOException {

        int result = in.read();
        if (result != -1) {
            count++;
        }
        return result;
    }

    @Override
    public int read(final byte[] b, final int off, final int len) throws IOException {

        int result = in.read(b, off, len);
        if (result != -1) {
            count += result;
        }
        return result;
    }

    @Override
    public long skip(final long n) throws IOException {
        long result = in.skip(n);
        count += result;
        return result;
    }

    @Override
    public synchronized void mark(final int readlimit) {
        in.mark(readlimit);
        mark = count;
        // it's okay to mark even if mark isn't supported, as reset won't work
    }

    @Override
    public synchronized void reset() throws IOException {
        if (!in.markSupported()) {
            throw new IOException("Mark not supported");
        }
        if (mark == -1) {
            throw new IOException("Mark not set");
        }

        in.reset();
        count = mark;
    }

    public final long getCount() {

        return count;
    }

    @Override
    public void close() throws IOException {
        super.close();

        // check for full file read
        long expected = file.getLength();
        if (file.isCompressed()) {
            if (file.containsKey(MongoFileConstants.storage.name())) {
                expected = file.getLong(MongoFileConstants.storage);
            }
            else if (file.containsKey(MongoFileConstants.compressedLength.name())) {
                expected = file.getLong(MongoFileConstants.compressedLength);
            }
        }

        if (expected != count) {
            throw new IOException("File Length mismatch");
        }

    }
}
