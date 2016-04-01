package me.lightspeed7.mongofs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

public class MongoFileConstantsTest {

    @Test
    public void testCoreGridFSList() {

        Set<String> coreFields = MongoFileConstants.getFields(false);
        assertNotNull(coreFields);
        assertEquals(9, coreFields.size());
        assertTrue(coreFields.contains(MongoFileConstants._id.name()));
        assertTrue(coreFields.contains(MongoFileConstants.filename.name()));
        assertTrue(coreFields.contains(MongoFileConstants.md5.name()));
        assertTrue(coreFields.contains(MongoFileConstants.chunkSize.name()));
        assertTrue(coreFields.contains(MongoFileConstants.contentType.name()));
        assertTrue(coreFields.contains(MongoFileConstants.aliases.name()));
        assertTrue(coreFields.contains(MongoFileConstants.length.name()));
        assertTrue(coreFields.contains(MongoFileConstants.uploadDate.name()));
        assertTrue(coreFields.contains(MongoFileConstants.metadata.name()));
    }

    @Test
    public void testCoreMongoFSList() {

        Set<String> coreFields = MongoFileConstants.getFields(true);
        assertNotNull(coreFields);
        assertEquals(20, coreFields.size());
        assertTrue(coreFields.contains(MongoFileConstants._id.name()));
        assertTrue(coreFields.contains(MongoFileConstants.filename.name()));
        assertTrue(coreFields.contains(MongoFileConstants.md5.name()));
        assertTrue(coreFields.contains(MongoFileConstants.chunkSize.name()));
        assertTrue(coreFields.contains(MongoFileConstants.contentType.name()));
        assertTrue(coreFields.contains(MongoFileConstants.aliases.name()));
        assertTrue(coreFields.contains(MongoFileConstants.length.name()));
        assertTrue(coreFields.contains(MongoFileConstants.uploadDate.name()));
        assertTrue(coreFields.contains(MongoFileConstants.metadata.name()));

        assertTrue(coreFields.contains(MongoFileConstants.chunkCount.name()));
        assertTrue(coreFields.contains(MongoFileConstants.compressionRatio.name())); // deprecated
        assertTrue(coreFields.contains(MongoFileConstants.ratio.name()));

        assertTrue(coreFields.contains(MongoFileConstants.compressedLength.name())); // deprecated
        assertTrue(coreFields.contains(MongoFileConstants.storage.name()));

        assertTrue(coreFields.contains(MongoFileConstants.compressionFormat.name())); // deprecated
        assertTrue(coreFields.contains(MongoFileConstants.format.name()));

        assertTrue(coreFields.contains(MongoFileConstants.expireAt.name()));
        assertTrue(coreFields.contains(MongoFileConstants.deleted.name()));

        assertTrue(coreFields.contains(MongoFileConstants.manifestId.name()));
        assertTrue(coreFields.contains(MongoFileConstants.manifestNum.name()));
    }
}
