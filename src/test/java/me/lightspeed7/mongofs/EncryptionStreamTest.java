package me.lightspeed7.mongofs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import me.lightspeed7.mongofs.MongoFile;
import me.lightspeed7.mongofs.MongoFileStore;
import me.lightspeed7.mongofs.MongoFileStoreConfig;
import me.lightspeed7.mongofs.MongoFileWriter;
import me.lightspeed7.mongofs.crypto.BasicCrypto;
import me.lightspeed7.mongofs.url.MongoFileUrl;
import me.lightspeed7.mongofs.util.BytesCopier;
import me.lightspeed7.mongofs.util.ChunkSize;

import org.junit.Before;
import org.junit.Test;

import com.mongodb.MongoClient;

public class EncryptionStreamTest {

    private static final String DB_NAME = "MongoFS-Encryption";

    private MongoFileStore store;

    private String bucketName = "excrypt";

    @Before
    public void before() {

        MongoClient mongoClient = MongoTestConfig.construct();

        MongoFileStoreConfig config = MongoFileStoreConfig.builder().bucket(bucketName) //
                .enableCompression(false).enableEncryption(new BasicCrypto(ChunkSize.tiny_4K)) //
                .build();

        store = new MongoFileStore(mongoClient.getDB(DB_NAME), config);

    }

    @Test
    public void testFullRead() throws Exception {

        // write a file
        MongoFileWriter writer = store.createNew("encrypt-test.txt", "text/plain");

        MongoFile file = writer.write(new ByteArrayInputStream(LoremIpsum.LOREM_IPSUM.getBytes()));
        assertNotNull(file);

        assertEquals(MongoFileUrl.ENCRYPTED, file.getURL().getFormat());

        // read the file
        ByteArrayOutputStream buf = new ByteArrayOutputStream(LoremIpsum.LOREM_IPSUM.length() * 2);
        file.readInto(buf, true);

        String result = buf.toString();
        assertNotNull(result);
        assertEquals(LoremIpsum.LOREM_IPSUM.length(), result.length());
        assertEquals(LoremIpsum.LOREM_IPSUM, result);

    }

    @Test
    public void testReadWithSkipAtBeginning() throws Exception {

        // write a file
        MongoFileWriter writer = store.createNew("encrypt-test.txt", "text/plain");

        MongoFile file = writer.write(new ByteArrayInputStream(LoremIpsum.LOREM_IPSUM.getBytes()));
        assertNotNull(file);

        assertEquals(MongoFileUrl.ENCRYPTED, file.getURL().getFormat());

        // read the file
        ByteArrayOutputStream buf = new ByteArrayOutputStream(LoremIpsum.LOREM_IPSUM.length() * 2);
        InputStream inputStream = file.getInputStream();
        inputStream.skip(9000);
        new BytesCopier(inputStream, buf).transfer(true);

        String result = buf.toString();
        assertNotNull(result);
        // assertEquals(LoremIpsum.LOREM_IPSUM.length() - 9000, result.length());
        assertEquals(LoremIpsum.LOREM_IPSUM.substring(9000), result);

    }

    @Test
    public void testReadWithSkipInTheMiddle() throws Exception {

        // write a file
        MongoFileWriter writer = store.createNew("encrypt-test.txt", "text/plain");

        MongoFile file = writer.write(new ByteArrayInputStream(LoremIpsum.LOREM_IPSUM.getBytes()));
        assertNotNull(file);

        assertEquals(MongoFileUrl.ENCRYPTED, file.getURL().getFormat());

        // read the file
        ByteArrayOutputStream buf = new ByteArrayOutputStream(LoremIpsum.LOREM_IPSUM.length() * 2);
        InputStream inputStream = file.getInputStream();
        BytesCopier copier = new BytesCopier(inputStream, buf);
        copier.transfer(3000, true);
        inputStream.skip(9000);
        copier.transfer(true);

        String result = buf.toString();
        assertNotNull(result);
        assertEquals(LoremIpsum.LOREM_IPSUM.length() - 9000, result.length());

        String compare = LoremIpsum.LOREM_IPSUM.substring(0, 3000) + LoremIpsum.LOREM_IPSUM.substring(12000);
        assertEquals(compare, result);

    }

}
