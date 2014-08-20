package me.lightspeed7.mongofs.writing;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.GeneralSecurityException;

import me.lightspeed7.mongofs.crypto.Crypto;

public class EncryptChunkOutputStream extends OutputStream {

    private DataOutputStream out;
    private Crypto crypto;

    public EncryptChunkOutputStream(Crypto crypto, final OutputStream out) {
        this.crypto = crypto;
        this.out = new DataOutputStream(out);
    }

    @Override
    public void write(final int b) throws IOException {
        throw new IllegalStateException("cannot write sinle bytes to this outputstream");
    }

    @Override
    public void write(final byte[] b, final int off, final int len) throws IOException {

        try {
            byte[] encrypt = crypto.encrypt(b, off, len);
            out.writeInt(len); // actual length
            out.writeInt(encrypt.length); // encrypted length
            out.write(encrypt, 0, encrypt.length);
        } catch (GeneralSecurityException e) {
            throw new IOException("Error in crypto", e);
        }
    }

    @Override
    public void close() throws IOException {

        out.close();
    }

}
