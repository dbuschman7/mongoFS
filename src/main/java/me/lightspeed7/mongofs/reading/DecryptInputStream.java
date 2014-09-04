package me.lightspeed7.mongofs.reading;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;

import me.lightspeed7.mongofs.MongoFile;
import me.lightspeed7.mongofs.crypto.Crypto;

public class DecryptInputStream extends InputStream {

    private Crypto crypto;
    private DataInputStream inputStream;
    private int offset = 0;
    private byte[] buffer = null;
    private long remainingBytes;

    public DecryptInputStream(final Crypto crypto, final MongoFile file, final InputStream inputStream) {
        this.crypto = crypto;
        this.inputStream = new DataInputStream(inputStream);
        this.remainingBytes = file.getStorageLength();
    }

    @Override
    public int available() {

        if (buffer == null) {
            return 0;
        }
        return buffer.length - offset;
    }

    @Override
    public int read() throws IOException {

        byte[] b = new byte[1];
        int res = read(b);
        if (res < 0) {
            return -1;
        }
        return b[0] & 0xFF;
    }

    @Override
    public int read(final byte[] b) throws IOException {

        return read(b, 0, b.length);
    }

    @Override
    public int read(final byte[] b, final int off, final int len) throws IOException {

        if (buffer == null || offset >= buffer.length) {
            if (remainingBytes <= 0) {
                return -1;
            }
            readEncryptedChunk(true);
        }

        int r = Math.min(len, buffer.length - offset);
        System.arraycopy(buffer, offset, b, off, r);
        offset += r;
        return r;

    }

    private int readEncryptedChunk(final boolean readActualHeader) throws IOException {
        int actualLength = readActualHeader ? inputStream.readInt() : -1;
        int chunkLength = inputStream.readInt();
        remainingBytes -= readActualHeader ? 8 : 4;

        byte[] temp = new byte[chunkLength];
        if (chunkLength != fillBuffer(temp, 0, chunkLength)) {
            throw new IllegalStateException("Unable to pull a full chunk of data from file");
        }

        // decrypt the chunk into the buffer
        try {
            buffer = crypto.decrypt(temp, 0, chunkLength);
        } catch (GeneralSecurityException e) {
            throw new IOException("Error decrypting data", e);
        }
        offset = 0;
        return actualLength;
    }

    private int fillBuffer(final byte[] temp, final int offset, final int length) throws IOException {
        int read = inputStream.read(temp, offset, length);
        remainingBytes -= read;
        if (length != read) {
            read += fillBuffer(temp, read, length - read); // recurse
        }
        return read;
    }

    /**
     * This will try to skip reading and decrypting as much data as possible
     */
    @Override
    public long skip(final long bytesToSkip) throws IOException {

        if (bytesToSkip <= 0) {
            return 0;
        }

        long stillToSkip = bytesToSkip;
        while (stillToSkip > 0) {
            if (buffer == null) { // no existing buffer, reading header and skip if possible
                int chunkLength = inputStream.readInt();
                remainingBytes -= 4;
                if (stillToSkip > chunkLength) { // skip the whole chunk
                    int length = inputStream.readInt();
                    long skipped = inputStream.skip(length);
                    remainingBytes -= skipped + 4;
                    stillToSkip -= chunkLength;
                }
                else { // only skipping part of this chunk, must read it
                    readEncryptedChunk(false);
                    offset = (int) stillToSkip; // the value must less than a chunk size
                    stillToSkip = 0;
                }
            }
            else { // handle the existing buffer
                int toSkip = (int) Math.min(stillToSkip, buffer.length - offset);
                offset += toSkip;
                remainingBytes -= toSkip;
                stillToSkip -= toSkip;
                if (buffer.length <= offset) { // reset the buffer since it is now empty
                    buffer = null;
                    offset = 0;
                }
            }

        }

        return bytesToSkip;
    }
}
