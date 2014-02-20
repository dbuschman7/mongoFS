package io.buschman.mongoFSPlus.gridFS;

import java.io.IOException;
import java.io.OutputStream;

/**
 * An output stream implementation that can be used to successively write to a GridFS file.
 * 
 * @author Guy K. Kloss
 */
class GridFSOutputStream extends OutputStream {

    private GridFSInputFile that;

    GridFSOutputStream(GridFSInputFile that) {

        this.that = that;
    }

    @Override
    public void write(final int b)
            throws IOException {

        byte[] byteArray = new byte[1];
        byteArray[0] = (byte) (b & 0xff);
        write(byteArray, 0, 1);
    }

    @Override
    public void write(final byte[] b, final int off, final int len)
            throws IOException {

        int offset = off;
        int length = len;
        int toCopy = 0;
        while (length > 0) {
            long chunkSize = that.getChunkSize();

            toCopy = length;
            if (toCopy > chunkSize - that.currentBufferPosition) {
                toCopy = (int) chunkSize - that.currentBufferPosition;
            }
            System.arraycopy(b, offset, that.buffer, that.currentBufferPosition, toCopy);
            that.currentBufferPosition += toCopy;
            offset += toCopy;
            length -= toCopy;
            if (that.currentBufferPosition == chunkSize) {
                that.dumpBuffer(false);
            }
        }
    }

    /**
     * Processes/saves all data from {@link java.io.InputStream} and closes the potentially present {@link java.io.OutputStream}.
     * The GridFS file will be persisted afterwards.
     */
    @Override
    public void close() {

        // write last buffer if needed
        that.dumpBuffer(true);
        // finish stream
        that.finishData();
        // save file obj
        that.superSave();
    }
}
