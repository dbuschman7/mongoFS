package me.lightspeed7.mongofs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import me.lightspeed7.mongofs.url.MongoFileUrl;
import me.lightspeed7.mongofs.url.StorageFormat;

import org.bson.types.ObjectId;
import org.junit.Test;

public class MongoFileTest {

    public static final String PDF = "application/pdf";
    public static final String ZIP = "application/zip";

    @Test
    public void testGZipFactoriesItemized() throws IOException {

        ObjectId id = new ObjectId();
        MongoFileUrl url = MongoFileUrl.construct(id, "fileName.pdf", PDF, StorageFormat.GZIPPED);
        assertNotNull(url);
        assertEquals(String.format("mongofile:gz:fileName.pdf?%s#application/pdf", id.toString()), url.getUrl().toString());

        assertEquals(id, url.getMongoFileId());
        assertEquals("fileName.pdf", url.getFilePath());
        assertEquals("fileName.pdf", url.getFileName());
        assertEquals("pdf", url.getExtension());
        assertTrue(url.isStoredCompressed());
        assertTrue(url.isDataCompressable());
        assertEquals(PDF, url.getMediaType());
    }

    @Test
    public void testFactoriesItemized() throws IOException {

        ObjectId id = new ObjectId();
        MongoFileUrl url = MongoFileUrl.construct(id, "fileName.zip", ZIP, StorageFormat.GRIDFS);
        assertNotNull(url);
        assertEquals(String.format("mongofile:fileName.zip?%s#application/zip", id.toString()), url.getUrl().toString());

        assertEquals(id, url.getMongoFileId());
        assertEquals("fileName.zip", url.getFilePath());
        assertEquals("fileName.zip", url.getFileName());
        assertEquals("zip", url.getExtension());
        assertFalse(url.isStoredCompressed());
        assertFalse(url.isDataCompressable());
        assertEquals(ZIP, url.getMediaType());
    }

    @Test
    public void testEncryptedFactoriesItemized() throws IOException {

        ObjectId id = new ObjectId();
        MongoFileUrl url = MongoFileUrl.construct(id, "fileName.zip", ZIP, StorageFormat.ENCRYPTED);
        assertNotNull(url);
        assertEquals(String.format("mongofile:enc:fileName.zip?%s#application/zip", id.toString()), url.getUrl().toString());

        assertEquals(id, url.getMongoFileId());
        assertEquals("fileName.zip", url.getFilePath());
        assertEquals("fileName.zip", url.getFileName());
        assertEquals("zip", url.getExtension());
        assertFalse(url.isStoredCompressed());
        assertFalse(url.isDataCompressable());
        assertTrue(url.isStoredEncrypted());
        assertEquals(ZIP, url.getMediaType());
    }

    @Test
    public void testBothFactoriesItemized() throws IOException {

        ObjectId id = new ObjectId();
        MongoFileUrl url = MongoFileUrl.construct(id, "fileName.pdf", PDF, StorageFormat.ECRYPTED_GZIP);
        assertNotNull(url);
        assertEquals(String.format("mongofile:encgz:fileName.pdf?%s#application/pdf", id.toString()), url.getUrl().toString());

        assertEquals(id, url.getMongoFileId());
        assertEquals("fileName.pdf", url.getFilePath());
        assertEquals("fileName.pdf", url.getFileName());
        assertEquals("pdf", url.getExtension());
        assertTrue(url.isStoredCompressed());
        assertTrue(url.isDataCompressable());
        assertTrue(url.isStoredEncrypted());
        assertEquals(PDF, url.getMediaType());
    }

    @Test
    public void testFactoriesFromSpecCrosswired() throws IOException {

        // this test to to test the ability to changes what MediaTypes are compressed
        // over time without problems for existing files already stored in the database

        // This file is not-compressed but compressable, yet is was not compressed
        MongoFileUrl url = MongoFileUrl
                .construct("mongofile:/home/oildex/x0064660/invoice/report/activeusers_19.PDF?52fb1e7b36707d6d13ebfda9#application/pdf");
        assertNotNull(url);

        assertEquals(new ObjectId("52fb1e7b36707d6d13ebfda9"), url.getMongoFileId());
        assertEquals("/home/oildex/x0064660/invoice/report/activeusers_19.PDF", url.getFilePath());
        assertEquals("activeusers_19.PDF", url.getFileName());
        assertEquals("pdf", url.getExtension());
        assertFalse(url.isStoredCompressed());
        assertTrue(url.isDataCompressable());

        assertEquals(PDF, url.getMediaType());
    }

    @Test
    public void testGZipFactoriesFromSpec() throws IOException {

        MongoFileUrl url = MongoFileUrl.construct("mongofile:/home/myself/foo/activeusers_19.ZIP?52fb1e7b36707d6d13ebfda9#application/zip");
        assertNotNull(url);

        assertEquals(new ObjectId("52fb1e7b36707d6d13ebfda9"), url.getMongoFileId());
        assertEquals("/home/myself/foo/activeusers_19.ZIP", url.getFilePath());
        assertEquals("activeusers_19.ZIP", url.getFileName());
        assertEquals("zip", url.getExtension());
        assertFalse(url.isStoredCompressed());
        assertFalse(url.isDataCompressable());

        assertEquals(ZIP, url.getMediaType());
    }

}
