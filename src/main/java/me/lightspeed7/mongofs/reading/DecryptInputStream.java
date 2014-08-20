package me.lightspeed7.mongofs.reading;

import java.io.DataInputStream;
import java.io.EOFException;
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

    public DecryptInputStream(Crypto crypto, MongoFile file, InputStream inputStream) {
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
            readEncryptedChunk();
        }

        int r = Math.min(len, buffer.length - offset);
        System.arraycopy(buffer, offset, b, off, r);
        offset += r;
        return r;

    }

    private void readEncryptedChunk() throws IOException, EOFException {
        int chunkLength = inputStream.readInt();
        remainingBytes -= 4;

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
    }

    private int fillBuffer(byte[] temp, int offset, int length) throws IOException, EOFException {
        int read = inputStream.read(temp, offset, length);
        remainingBytes -= read;
        if (length != read) {
            read += fillBuffer(temp, read, length - read); // recurse
        }
        return read;
    }

    /**
     * THis will read all the data skipped due to the fact that it is compressed
     */
    @Override
    public long skip(final long bytesToSkip) throws IOException {

        if (bytesToSkip <= 0) {
            return 0;
        }

        if (buffer == null) {
            readEncryptedChunk();
        }

        int leftInBuffer = buffer.length - offset;
        int bytesSkipped = leftInBuffer;
        if (bytesToSkip < leftInBuffer) {
            // within my buffer
            offset += bytesToSkip;
        }
        else {
            // more than my buffer
            long leftToSkip = bytesToSkip - leftInBuffer;
            while (leftToSkip > 0) {
                readEncryptedChunk();

                if (leftToSkip < buffer.length) { // last buffer to skip
                    offset += leftToSkip;
                    bytesSkipped += leftToSkip;
                    leftToSkip = 0; // need to jump out here
                }
                else {
                    leftToSkip -= buffer.length;
                    bytesSkipped = buffer.length;
                }
            }
        }
        return bytesSkipped;
    }
}
