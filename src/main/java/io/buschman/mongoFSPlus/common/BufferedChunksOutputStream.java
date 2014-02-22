package io.buschman.mongoFSPlus.common;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * The class implements a buffered output stream. By setting up such an output stream, an application can write bytes to the
 * underlying output stream in an orderly chunked fashion
 * 
 * NOTE: I have removed the synchronized from the write and flush methods for performance reasons, thread safety is not required.
 * 
 * @author David Buschman
 * @author Arthur van Hoff - Original
 * 
 * @since JDK1.0
 */
public class BufferedChunksOutputStream extends FilterOutputStream {

    /**
     * The internal buffer where the chunk data is stored.
     */
    protected byte buf[];

    /**
     * The number of valid bytes in the buffer. This value is always in the range <tt>0</tt> through <tt>buf.length</tt>; elements
     * <tt>buf[0]</tt> through <tt>buf[count-1]</tt> contain valid byte data.
     */
    protected int currentPosition;

    /**
     * The size of the chunk to be written downstream
     */
    private int chunkSize;

    /**
     * Creates a new buffered output stream to write data to the specified underlying output stream.
     * 
     * @param out
     *            the underlying output stream.
     */
    public BufferedChunksOutputStream(OutputStream out) {

        this(out, 8192);
    }

    /**
     * Creates a new buffered output stream to write data to the specified underlying output stream with the specified buffer
     * size.
     * 
     * @param out
     *            the underlying output stream.
     * @param chunkSize
     *            the buffer size.
     * @exception IllegalArgumentException
     *                if size &lt;= 0.
     */
    public BufferedChunksOutputStream(OutputStream out, int chunkSize) {

        super(out);
        this.chunkSize = chunkSize;
        if (chunkSize <= 0) {
            throw new IllegalArgumentException("Buffer size <= 0");
        }
        buf = new byte[chunkSize];
    }

    /** Flush the internal buffer */
    private void flushBuffer()
            throws IOException {

        if (currentPosition > 0) {
            out.write(buf, 0, currentPosition);
            currentPosition = 0;
        }
    }

    /**
     * Writes the specified byte to this buffered output stream.
     * 
     * @param b
     *            the byte to be written.
     * @exception IOException
     *                if an I/O error occurs.
     */
    public void write(int b)
            throws IOException {

        if (currentPosition >= this.chunkSize) {
            flushBuffer();
        }
        buf[currentPosition++] = (byte) b;
        if (currentPosition >= this.chunkSize) {
            flushBuffer();
        }
    }

    /**
     * Writes <code>len</code> bytes from the specified byte array starting at offset <code>off</code> to this buffered output
     * stream.
     * 
     * This method will recurse on itself until the buffer is exhausted to allow for orderly chunk writes to the underlying
     * stream.
     * 
     * @param b
     *            the data.
     * @param off
     *            the start offset in the data.
     * @param len
     *            the number of bytes to write.
     * @exception IOException
     *                if an I/O error occurs.
     */
    public void write(byte b[], int off, int len)
            throws IOException {

        if (len <= 0) {
            return;
        }

        // if empty buffer and full chunk copy, then send straight to underlying
        // if not-empty buffer and enough to fill, then copy to buffer and flush
        // if not empty and not enough to fill, then copy to buffer and return
        int toCopy = len;
        if (toCopy >= this.chunkSize - currentPosition) {
            if (this.currentPosition == 0) {
                // write straight to the underlying stream
                out.write(b, off, this.chunkSize);
                write(b, off + chunkSize, len - chunkSize); // recurse
            } else {
                // fill the rest of the buffer and flush
                toCopy = this.chunkSize - currentPosition;
                System.arraycopy(b, off, buf, currentPosition, toCopy);
                currentPosition += toCopy;
                // test for full buffer
                assert currentPosition == this.chunkSize;
                flushBuffer();
                write(b, off + toCopy, len - toCopy); // recurse
            }
        } else {
            // the last of the buffer
            if (toCopy > 0) {
                System.arraycopy(b, off, buf, currentPosition, toCopy);
                currentPosition += toCopy;
            }
        }
    }

    /**
     * Flushes this buffered output stream. This forces any buffered output bytes to be written out to the underlying output
     * stream.
     * 
     * @exception IOException
     *                if an I/O error occurs.
     * @see java.io.FilterOutputStream#out
     */
    public void flush()
            throws IOException {

        flushBuffer();
        out.flush();
    }
}
