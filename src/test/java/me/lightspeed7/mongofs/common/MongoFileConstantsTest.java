package me.lightspeed7.mongofs.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

public class MongoFileConstantsTest {

    @Test
    public void testCoreGridFSList() {

        Set<String> coreFields = MongoFileConstants.getCoreFields(false);
        assertNotNull(coreFields);
        assertEquals(8, coreFields.size());
        assertTrue(coreFields.contains(MongoFileConstants._id.name()));
        assertTrue(coreFields.contains(MongoFileConstants.filename.name()));
        assertTrue(coreFields.contains(MongoFileConstants.md5.name()));
        assertTrue(coreFields.contains(MongoFileConstants.chunkSize.name()));
        assertTrue(coreFields.contains(MongoFileConstants.contentType.name()));
        assertTrue(coreFields.contains(MongoFileConstants.aliases.name()));
        assertTrue(coreFields.contains(MongoFileConstants.length.name()));
        assertTrue(coreFields.contains(MongoFileConstants.uploadDate.name()));
    }

    @Test
    public void testCoreMongoFSList() {

        Set<String> coreFields = MongoFileConstants.getCoreFields(true);
        assertNotNull(coreFields);
        assertEquals(12, coreFields.size());
        assertTrue(coreFields.contains(MongoFileConstants._id.name()));
        assertTrue(coreFields.contains(MongoFileConstants.filename.name()));
        assertTrue(coreFields.contains(MongoFileConstants.md5.name()));
        assertTrue(coreFields.contains(MongoFileConstants.chunkSize.name()));
        assertTrue(coreFields.contains(MongoFileConstants.contentType.name()));
        assertTrue(coreFields.contains(MongoFileConstants.aliases.name()));
        assertTrue(coreFields.contains(MongoFileConstants.length.name()));
        assertTrue(coreFields.contains(MongoFileConstants.uploadDate.name()));

        assertTrue(coreFields.contains(MongoFileConstants.chunkCount.name()));
        assertTrue(coreFields.contains(MongoFileConstants.compressionRatio.name()));
        assertTrue(coreFields.contains(MongoFileConstants.compressedLength.name()));
    }
}
