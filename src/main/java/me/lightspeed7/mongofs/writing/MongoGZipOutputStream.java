package me.lightspeed7.mongofs.writing;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import me.lightspeed7.mongofs.common.InputFile;
import me.lightspeed7.mongofs.common.MongoFileConstants;

/**
 * 
 * @author David Buschman
 * 
 */
public class MongoGZipOutputStream extends OutputStream {

    private InputFile inputFile;
    private OutputStream surrogate;

    public MongoGZipOutputStream(InputFile inputFile, OutputStream given)
            throws IOException {

        // This chain is : me -> before -> compression -> after -> given
        //
        // It will be constructed in reverse
        //
        CountingOutputStream after = new CountingOutputStream(MongoFileConstants.compressedLength, inputFile, given);
        GZIPOutputStream compression = new GZIPOutputStream(after);
        CountingOutputStream before = new CountingOutputStream(MongoFileConstants.length, inputFile, compression);

        this.surrogate = before;
        this.inputFile = inputFile;
    }

    @Override
    public void write(int b)
            throws IOException {

        this.surrogate.write(b);
    }

    @Override
    public void write(byte[] b)
            throws IOException {

        this.surrogate.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len)
            throws IOException {

        this.surrogate.write(b, off, len);
    }

    @Override
    public void flush()
            throws IOException {

        this.surrogate.flush();
    }

    @Override
    public void close()
            throws IOException {

        // flush and close the streams
        try {
            this.surrogate.close();
        } finally {

            int length = Integer.parseInt(inputFile.get(MongoFileConstants.length.toString()).toString());
            int compressed = Integer.parseInt(inputFile.get(MongoFileConstants.compressedLength.toString()).toString());

            double ratio = 0.0d;
            if (length > 0) {
                ratio = (double) compressed / length;
            }

            inputFile.put(MongoFileConstants.compressionRatio.toString(), ratio);
        }
    }
}
