package me.lightspeed7.mongofs.writing;

import java.io.IOException;
import java.io.OutputStream;

import me.lightspeed7.mongofs.MongoFile;
import me.lightspeed7.mongofs.common.MongoFileConstants;

public class CountingOutputStream extends OutputStream {

    long count = 0;
    private MongoFileConstants key;
    private MongoFile inputFile;
    private OutputStream out;

    public CountingOutputStream(MongoFileConstants key, MongoFile inputFile, OutputStream out) {

        this.key = key;
        this.inputFile = inputFile;
        this.out = out;
    }

    @Override
    public void write(int b)
            throws IOException {

        out.write(b);
        ++count;
    }

    @Override
    public void write(byte[] b, int off, int len)
            throws IOException {

        out.write(b, off, len);
        count += len;
    }

    @Override
    public void close()
            throws IOException {

        out.close();

        inputFile.put(key.toString(), count);
        inputFile.save();
    }
}
