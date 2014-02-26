package me.lightspeed7.mongofs.reading;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.mongodb.util.Util;

public class CountingInputStream extends FilterInputStream {

    private long count = 0;
    private MessageDigest messageDigest;
    private String md5;

    public CountingInputStream(InputStream given) {

        super(given);

        try {
            this.messageDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("No MD5!");
        }
    }

    @Override
    public int read()
            throws IOException {

        int read = super.read();
        if (read > 0) {
            count += read;
            messageDigest.update((byte) read);
        }
        return read;
    }

    @Override
    public int read(byte[] b, int off, int len)
            throws IOException {

        int read = super.read(b, off, len);
        if (read > 0) {
            count += read;
            messageDigest.update(b, off, read);
        }
        return read;
    }

    public final long getCount() {

        return this.count;
    }

    public String getDigest() {

        if (this.md5 == null) {
            this.md5 = Util.toHex(messageDigest.digest());
        }
        return this.md5;
    }
}
