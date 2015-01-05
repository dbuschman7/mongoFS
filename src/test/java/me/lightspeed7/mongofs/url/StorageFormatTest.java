package me.lightspeed7.mongofs.url;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class StorageFormatTest {

    @Test
    public void testGRIDFS() {
        assertEquals(null, StorageFormat.GRIDFS.getCode());
        assertFalse(StorageFormat.GRIDFS.isCompressed());
        assertFalse(StorageFormat.GRIDFS.isEncrypted());
    }

    @Test
    public void testGZIPPED() {
        assertEquals("gz", StorageFormat.GZIPPED.getCode());
        assertTrue(StorageFormat.GZIPPED.isCompressed());
        assertFalse(StorageFormat.GZIPPED.isEncrypted());
    }

    @Test
    public void testENCRYPTED() {
        assertEquals("enc", StorageFormat.ENCRYPTED.getCode());
        assertFalse(StorageFormat.ENCRYPTED.isCompressed());
        assertTrue(StorageFormat.ENCRYPTED.isEncrypted());
    }

    @Test
    public void testECRYPTEDGZIP() {
        assertEquals("encgz", StorageFormat.ENCRYPTED_GZIP.getCode());
        assertTrue(StorageFormat.ENCRYPTED_GZIP.isCompressed());
        assertTrue(StorageFormat.ENCRYPTED_GZIP.isEncrypted());
    }
}
