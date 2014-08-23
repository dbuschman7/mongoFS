package me.lightspeed7.mongofs.crypto;

import java.security.GeneralSecurityException;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import me.lightspeed7.mongofs.util.ChunkSize;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BasicCrypto implements Crypto {

    private static final String SALT = "SayNoToOracle";

    private static final char[] PASSWORD = "MongoDB Rules The World!".toCharArray();

    private static final String CIHPER_NAME = "AES";

    private static final Logger LOG = LoggerFactory.getLogger(Crypto.class);

    private Cipher cipher;

    private Cipher decipher;

    private ChunkSize chunkSize = ChunkSize.small_32K;

    public BasicCrypto() {
        try {
            // Create key
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            KeySpec spec = new PBEKeySpec(PASSWORD, SALT.getBytes("UTF-8"), 1024, 128);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKeySpec secret = new SecretKeySpec(tmp.getEncoded(), "AES");

            cipher = Cipher.getInstance(CIHPER_NAME);
            cipher.init(Cipher.ENCRYPT_MODE, secret);

            decipher = Cipher.getInstance(CIHPER_NAME);
            decipher.init(Cipher.DECRYPT_MODE, secret);

        } catch (Throwable t) {
            LOG.error(String.format("Unable to initialize %s crypto", "AES"));
        }
    }

    public BasicCrypto(final ChunkSize chunkSize) {
        this();
        this.chunkSize = chunkSize;
    }

    @Override
    public byte[] encrypt(final byte[] dataIn, final int offset, final int length) throws GeneralSecurityException {
        return cipher.doFinal(dataIn, offset, length);
    }

    @Override
    public byte[] decrypt(final byte[] dataIn, final int offset, final int length) throws GeneralSecurityException {
        return decipher.doFinal(dataIn, offset, length);
    }

    @Override
    public ChunkSize getChunkSize() {
        return this.chunkSize;
    }
}
