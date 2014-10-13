package me.lightspeed7.mongofs;

import java.io.IOException;
import java.io.OutputStream;

public class MongoEncryptionOutputStream extends OutputStream {

    private MongoFile inputFile;
    private OutputStream surrogate;

    public MongoEncryptionOutputStream(final MongoFileStoreConfig config, final MongoFile inputFile, final OutputStream given)
            throws IOException {

        // This chain is : me -> before -> chunking -> encryption -> after -> given
        //
        // It will be constructed in reverse
        //
        CountingOutputStream after = new CountingOutputStream(MongoFileConstants.storage, inputFile, given);
        EncryptChunkOutputStream encryption = new EncryptChunkOutputStream(config.getEncryption(), after);
        BufferedChunksOutputStream chunking = new BufferedChunksOutputStream(encryption, config.getEncryption().getChunkSize());
        CountingOutputStream before = new CountingOutputStream(MongoFileConstants.length, inputFile, chunking);

        this.surrogate = before;
        this.inputFile = inputFile;
    }

    @Override
    public void write(final int b) throws IOException {

        this.surrogate.write(b);
    }

    @Override
    public void write(final byte[] b) throws IOException {

        this.surrogate.write(b);
    }

    @Override
    public void write(final byte[] b, final int off, final int len) throws IOException {

        this.surrogate.write(b, off, len);
    }

    @Override
    public void flush() throws IOException {

        this.surrogate.flush();
    }

    @Override
    public void close() throws IOException {

        // flush and close the streams
        try {
            this.surrogate.close();
        } catch (IOException e) {
            throw e; // re-throw it
        } catch (Throwable t) {
            throw new RuntimeException("Unhandled exception caught", t);
        } finally {

            long length = inputFile.getLong(MongoFileConstants.length, 0);
            long compressed = inputFile.getLong(MongoFileConstants.storage, 0);

            double ratio = 0.0d;
            if (length > 0) {
                ratio = (double) compressed / length;
            }

            inputFile.put(MongoFileConstants.ratio.toString(), ratio);
            inputFile.save();
        }
    }
}
