package me.lightspeed7.mongofs.zip;

import java.io.IOException;
import java.io.InputStream;

public class LogItInputStream extends InputStream {

    private InputStream surrogate;

    public LogItInputStream(InputStream input) {
        this.surrogate = input;
        log("logging class", input.getClass().getName());
    }

    @Override
    public int read() throws IOException {
        // log("read()");
        return surrogate.read();
    }

    private void log(String method, Object... args) {
        StringBuilder buf = new StringBuilder();
        buf.append(method).append(" - ");
        if (args != null) {
            for (Object object : args) {
                buf.append(object.toString()).append("   ");
            }
        }

        System.out.println(buf.toString());
    }

    @Override
    public int read(byte[] b) throws IOException {
        log("read(byte[] b)", 0, b.length);
        return super.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        log("read(byte[] b, int off, int len)", off, len);
        return super.read(b, off, len);
    }

    @Override
    public long skip(long n) throws IOException {
        log("skip(long n)", n);
        return super.skip(n);
    }

    @Override
    public int available() throws IOException {
        int avail = super.available();
        log("available()", avail);
        return avail;
    }

    @Override
    public void close() throws IOException {
        log("close()");
        super.close();
    }

    @Override
    public synchronized void mark(int readlimit) {
        log("mark(int readlimit)", readlimit);
        super.mark(readlimit);
    }

    @Override
    public synchronized void reset() throws IOException {
        log("reset()");
        super.reset();
    }

    @Override
    public boolean markSupported() {
        boolean suppoerted = super.markSupported();
        log("markSupported()", suppoerted);
        return suppoerted;
    }

}
