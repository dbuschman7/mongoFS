package me.lightspeed7.mongofs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import me.lightspeed7.mongofs.url.StorageFormat;

import org.junit.Test;

public class StorageFormatTest {

    @Test
    public void testGridFS() {

        assertEquals(StorageFormat.GRIDFS, StorageFormat.find("GRIDFS"));
        assertFalse(StorageFormat.GRIDFS.isCompressed());
        assertFalse(StorageFormat.GRIDFS.isEncrypted());
        assertNull(StorageFormat.GRIDFS.getCode());
    }

    @Test
    public void testGZIPPED() {

        assertEquals(StorageFormat.GZIPPED, StorageFormat.find("GZIPPED"));
        assertTrue(StorageFormat.GZIPPED.isCompressed());
        assertFalse(StorageFormat.GZIPPED.isEncrypted());
        assertEquals("gz", StorageFormat.GZIPPED.getCode());
    }

    @Test
    public void testENCRYPTED() {

        assertEquals(StorageFormat.ENCRYPTED, StorageFormat.find("ENCRYPTED"));
        assertFalse(StorageFormat.ENCRYPTED.isCompressed());
        assertTrue(StorageFormat.ENCRYPTED.isEncrypted());
        assertEquals("enc", StorageFormat.ENCRYPTED.getCode());
    }

    @Test
    public void testENCRYPTEDGZIP() {

        assertEquals(StorageFormat.ENCRYPTED_GZIP, StorageFormat.find("ENCRYPTED_GZIP"));
        assertTrue(StorageFormat.ENCRYPTED_GZIP.isCompressed());
        assertTrue(StorageFormat.ENCRYPTED_GZIP.isEncrypted());
        assertEquals("encgz", StorageFormat.ENCRYPTED_GZIP.getCode());
    }

    @Test
    public void testOtherStuff() {
        assertEquals(StorageFormat.GRIDFS, StorageFormat.find(null));
        assertNull(StorageFormat.find("foo"));

        assertEquals(StorageFormat.ENCRYPTED_GZIP, StorageFormat.find("encgz"));

    }

    @Test
    public void testDetect() {

        assertEquals(StorageFormat.GRIDFS, StorageFormat.detect(false, false));
        assertEquals(StorageFormat.GZIPPED, StorageFormat.detect(true, false));
        assertEquals(StorageFormat.ENCRYPTED, StorageFormat.detect(false, true));
        assertEquals(StorageFormat.ENCRYPTED_GZIP, StorageFormat.detect(true, true));
    }

}
