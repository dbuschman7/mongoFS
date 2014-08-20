package me.lightspeed7.mongofs.writing;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import me.lightspeed7.mongofs.LoremIpsum;
import me.lightspeed7.mongofs.crypto.BasicCrypto;

import org.junit.Test;

public class EncryptionTest {

    private static final me.lightspeed7.mongofs.crypto.Crypto CRYPTO = new BasicCrypto();

    @Test
    public void test() throws Exception {

        String raw = LoremIpsum.LOREM_IPSUM;
        assertEquals(32087, raw.length());

        byte[] bytes = raw.getBytes();
        byte[] encrypted = CRYPTO.encrypt(bytes, 0, bytes.length);
        assertEquals(32096, encrypted.length);

        String backToRaw = new String(CRYPTO.decrypt(encrypted, 0, encrypted.length));
        assertEquals(32087, backToRaw.length());
    }

    @Test
    public void testBatching() throws Exception {
        ByteArrayOutputStream buf = new ByteArrayOutputStream(LoremIpsum.LOREM_IPSUM.length() * 2);
        DataOutputStream out = new DataOutputStream(buf);

        String raw = LoremIpsum.LOREM_IPSUM;
        int batch = 10000;

        // WRITE
        while (raw.length() > 0) {
            int size = raw.length() > batch ? batch : raw.length();
            String temp = raw.substring(0, size);
            assert temp.length() == size;
            raw = raw.substring(size);

            // encrypt
            byte[] encrypted = temp.getBytes();
            byte[] bytes = CRYPTO.encrypt(encrypted, 0, encrypted.length);
            out.writeShort(bytes.length);
            out.write(bytes, 0, bytes.length);
        }

        // READ
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(buf.toByteArray()));
        StringBuilder inBuf = new StringBuilder(LoremIpsum.LOREM_IPSUM.length());
        while (in.available() > 0) {
            short size = in.readShort();
            byte[] temp = new byte[size];

            in.read(temp, 0, size);
            String decrypt = new String(CRYPTO.decrypt(temp, 0, temp.length));
            inBuf.append(decrypt);
        }

        assertEquals(LoremIpsum.LOREM_IPSUM, inBuf.toString());
    }
}
