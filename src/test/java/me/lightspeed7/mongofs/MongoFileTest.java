package me.lightspeed7.mongofs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import com.google.common.net.MediaType;

public class MongoFileTest {

    @Test
    public void testGZipFactoriesItemized()
            throws IOException {

        MongoFileUrl url = MongoFileUrl.construct("id", "fileName.pdf", MediaType.PDF.toString(), true);
        assertNotNull(url);
        assertEquals("mongofile:gz:fileName.pdf?id#application/pdf", url.getUrl().toString());

        assertEquals("id", url.getMongoFileId());
        assertEquals("fileName.pdf", url.getFilePath());
        assertEquals("fileName.pdf", url.getFileName());
        assertEquals("pdf", url.getExtension());
        assertTrue(url.isStoredCompressed());
        assertFalse(url.isDataCompressed());
        assertEquals(MediaType.PDF.toString(), url.getMediaType());
    }

    @Test
    public void testFactoriesItemized()
            throws IOException {

        MongoFileUrl url = MongoFileUrl.construct("id", "fileName.zip", MediaType.ZIP.toString(), true);
        assertNotNull(url);
        assertEquals("mongofile:fileName.zip?id#application/zip", url.getUrl().toString());

        assertEquals("id", url.getMongoFileId());
        assertEquals("fileName.zip", url.getFilePath());
        assertEquals("fileName.zip", url.getFileName());
        assertEquals("zip", url.getExtension());
        assertFalse(url.isStoredCompressed());
        assertTrue(url.isDataCompressed());
        assertEquals(MediaType.ZIP.toString(), url.getMediaType());
    }

    @Test
    public void testFactoriesFromSpecCrosswired()
            throws IOException {

        // this test to to test the ability to changes what MediaTypes are compressed
        // over time without problems for existing files already stored in the database

        // This file is not-compressed but compressable, yet is was not compressed
        MongoFileUrl url = MongoFileUrl
                .construct("mongofile:/home/oildex/x0064660/invoice/report/activeusers_19.PDF?52fb1e7b36707d6d13ebfda9#application/pdf");
        assertNotNull(url);

        assertEquals("52fb1e7b36707d6d13ebfda9", url.getMongoFileId());
        assertEquals("/home/oildex/x0064660/invoice/report/activeusers_19.PDF", url.getFilePath());
        assertEquals("activeusers_19.PDF", url.getFileName());
        assertEquals("pdf", url.getExtension());
        assertFalse(url.isStoredCompressed());
        assertFalse(url.isDataCompressed());

        assertEquals(MediaType.PDF.toString(), url.getMediaType());
    }

    @Test
    public void testGZipFactoriesFromSpec()
            throws IOException {

        MongoFileUrl url = MongoFileUrl
                .construct("mongofile:/home/myself/foo/activeusers_19.ZIP?52fb1e7b36707d6d13ebfda9#application/zip");
        assertNotNull(url);

        assertEquals("52fb1e7b36707d6d13ebfda9", url.getMongoFileId());
        assertEquals("/home/myself/foo/activeusers_19.ZIP", url.getFilePath());
        assertEquals("activeusers_19.ZIP", url.getFileName());
        assertEquals("zip", url.getExtension());
        assertFalse(url.isStoredCompressed());
        assertTrue(url.isDataCompressed());

        assertEquals(MediaType.ZIP.toString(), url.getMediaType());
    }

}
