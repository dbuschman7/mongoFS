package me.lightspeed7.mongofs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

import me.lightspeed7.mongofs.crypto.BasicCrypto;
import me.lightspeed7.mongofs.url.MongoFileUrl;
import me.lightspeed7.mongofs.url.StorageFormat;
import me.lightspeed7.mongofs.util.BytesCopier;
import me.lightspeed7.mongofs.util.ChunkSize;

import org.bson.types.ObjectId;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mongodb.Document;
import org.mongodb.MongoDatabase;

import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;

public class MongoFileStoreTest {

	private static final String DB_NAME = "MongoFSTest-fileStore";

	private static MongoDatabase database;

	private static MongoClient mongoClient;

	// initializer
	@BeforeClass
	public static void initial() {
		mongoClient = MongoTestConfig.construct();

		mongoClient.dropDatabase(DB_NAME);
		database = new MongoDatabase(mongoClient.getDB(DB_NAME));
	}

	@Test
	public void testBasicUncompressedRoundTrip() throws IOException {

		doRoundTrip("loremIpsum.txt", MongoFileStoreConfig.DEFAULT_CHUNKSIZE, false, false);
	}

	@Test
	public void testBasicCompressedRoundTrip() throws IOException {

		doRoundTrip("loremIpsum.txt", MongoFileStoreConfig.DEFAULT_CHUNKSIZE, true, false);
	}

	@Test
	public void testLotsOfChunksUncompressedRoundTrip() throws IOException {

		doRoundTrip("loremIpsum.txt", ChunkSize.tiny_4K, false, false);
	}

	@Test
	public void testLotsOfChunksCompressedRoundTrip() throws IOException {

		doRoundTrip("loremIpsum.txt", ChunkSize.tiny_4K, true, false);
	}

	@Test
	public void testLotsOfChunksEncryptedRoundTrip() throws IOException {

		doRoundTrip("loremIpsum.txt", ChunkSize.tiny_4K, false, true);
	}

	@Test
	public void testLotsOfChunksNoCompNoEncryptRoundTrip() throws IOException {

		doRoundTrip("loremIpsum.txt", ChunkSize.tiny_4K, false, false);
	}

	@Test
	public void testLotsOfChunksEncryptedAndCompressedRoundTrip() throws IOException {

		doRoundTrip("loremIpsum.txt", ChunkSize.tiny_4K, true, true);
	}

	@Test
	public void testSingleChunkEncryptedAndCompressedRoundTrip() throws IOException {

		doRoundTrip("loremIpsum.txt", ChunkSize.large_1M, true, true);
	}

	@Test
	public void testUpload() throws IOException {

		MongoFileStoreConfig config = MongoFileStoreConfig.builder()//
				.bucket("mongofs").chunkSize(ChunkSize.medium_256K)//
				.enableCompression(true).enableEncryption(new BasicCrypto())//
				.writeConcern(WriteConcern.SAFE) //
				.build();
		MongoFileStore store = new MongoFileStore(database, config);

		ByteArrayInputStream in = new ByteArrayInputStream(LoremIpsum.LOREM_IPSUM.getBytes());
		MongoFile mongoFile = store.upload("loremIpsum.txt", "test/plain", null, false, in);
		assertNotNull(mongoFile);

		assertEquals(32087, mongoFile.getLength());

	}

	@Test(expected = IllegalStateException.class)
	public void testGetInputStreamThrows() throws Exception {
		MongoFileUrl url = MongoFileUrl.construct("mongofile:enc:fileName.pdf?52fb1e7b36707d6d13ebfda9#application/pdf");

		MongoFileStoreConfig config = MongoFileStoreConfig.builder()//
				.bucket("mongofs").enableEncryption(null)//
				.writeConcern(null).readPreference(null)//
				.build();
		MongoFileStore store = new MongoFileStore(database, config);

		new MongoFile(store, url, 1234).getInputStream();
	}

	@Test
	public void testFindOneReturnsNull() {

		MongoFileStoreConfig config = MongoFileStoreConfig.builder()//
				.bucket("mongofs").enableEncryption(null)//
				.writeConcern(null).readPreference(null)//
				.build();
		MongoFileStore store = new MongoFileStore(database, config);

		assertNull(store.findOne(new ObjectId()));

	}

	@Test
	public void testRemoveWithQuery() {

		MongoFileStoreConfig config = MongoFileStoreConfig.builder()//
				.bucket("mongofs").enableEncryption(null)//
				.writeConcern(null).readPreference(null)//
				.build();
		MongoFileStore store = new MongoFileStore(database, config);

		store.remove(new Document(MongoFileConstants._id.name(), new ObjectId()));

	}

	@Test
	public void testGetManifestReturnsNull() throws Exception {

		MongoFileStoreConfig config = MongoFileStoreConfig.builder()//
				.bucket("mongofs").enableEncryption(null)//
				.writeConcern(null).readPreference(null)//
				.build();
		MongoFileStore store = new MongoFileStore(database, config);

		MongoFileUrl url = MongoFileUrl
				.construct("mongofile:/home/oildex/x0064660/invoice/report/activeusers_19.PDF?52fb1e7b36707d6d13ebfda9#application/pdf");

		assertNull(store.getManifest(url));

	}

	//
	// internal
	// /////////////////

	private void doRoundTrip(final String filename, final ChunkSize chunkSize, final boolean compress, final boolean encrypt)
			throws IOException {

		MongoFileStoreConfig config = MongoFileStoreConfig.builder()//
				.bucket("fileStore")//
				.chunkSize(chunkSize)//
				.enableCompression(compress)//
				.enableEncryption(encrypt ? new BasicCrypto(chunkSize) : null)//
				.writeConcern(WriteConcern.SAFE) //
				.build();
		MongoFileStore store = new MongoFileStore(database, config);

		MongoFileWriter writer = store.createNew(filename, "text/plain", null, compress);
		ByteArrayInputStream in = new ByteArrayInputStream(LoremIpsum.LOREM_IPSUM.getBytes());
		OutputStream out = writer.getOutputStream();
		try {
			new BytesCopier(in, out).transfer(true);
		} finally {
			out.close();
		}

		// verify it exists
		MongoFile mongoFile = writer.getMongoFile();
		assertTrue(store.exists(mongoFile.getURL()));
		assertNotNull(mongoFile.getMD5());

		// read a file
		assertEquals(compress, mongoFile.getURL().isStoredCompressed());
		assertEquals(LoremIpsum.LOREM_IPSUM.length(), mongoFile.getLength());
		if (compress && encrypt) {
			assertEquals(StorageFormat.ENCRYPTED_GZIP.getCode(), mongoFile.get(MongoFileConstants.format)); // verify compression
			assertNotNull(mongoFile.get(MongoFileConstants.storage)); // verify compression
			assertNotNull(mongoFile.get(MongoFileConstants.ratio)); // verify compression
		} else if (compress) {
			assertEquals(StorageFormat.GZIPPED.getCode(), mongoFile.get(MongoFileConstants.format)); // verify compression
			assertNotNull(mongoFile.get(MongoFileConstants.storage)); // verify compression
			assertNotNull(mongoFile.get(MongoFileConstants.ratio)); // verify compression
		} else if (encrypt) {
			assertEquals(StorageFormat.ENCRYPTED.getCode(), mongoFile.get(MongoFileConstants.format)); // verify encryption
			assertNotNull(mongoFile.get(MongoFileConstants.storage)); // verify encryption
			assertNotNull(mongoFile.get(MongoFileConstants.ratio)); // verify encryption sets its ratio
		} else {
			assertNull(mongoFile.get(MongoFileConstants.storage)); // verify no compression
			assertNull(mongoFile.get(MongoFileConstants.format)); // verify no compression
			assertNull(mongoFile.get(MongoFileConstants.ratio)); // verify no compression
		}

		ByteArrayOutputStream out2 = new ByteArrayOutputStream(32 * 1024);
		new BytesCopier(new MongoFileReader(store, mongoFile).getInputStream(), out2).transfer(true);
		assertEquals(LoremIpsum.LOREM_IPSUM, out2.toString());

	}

	@Test
	public void testUpload1() throws IOException {
		MongoFileStore store = new MongoFileStore(database, MongoFileStoreConfig.builder().build());
		assertNotNull(store.toString());

		MongoFile file = store.upload(LoremIpsum.getFile(), "text/plain");
		assertNotNull(file);
		assertEquals(LoremIpsum.LOREM_IPSUM.length(), file.getLength());
		assertTrue(store.exists(file.getURL()));
		assertTrue(store.exists(file.getId()));

		MongoFile file2 = store.findOne(file.getURL().getUrl());
		assertNotNull(file2);

	}

	@Test
	public void testUpload2() throws IOException {
		MongoFileStore store = new MongoFileStore(database, MongoFileStoreConfig.builder().build());
		assertNotNull(store.toString());

		ByteArrayInputStream inputStream = new ByteArrayInputStream(LoremIpsum.getBytes());
		try {
			MongoFile file = store.upload(LoremIpsum.getFile().getAbsolutePath(), "text/plain", inputStream);
			assertNotNull(file);
			assertEquals(LoremIpsum.LOREM_IPSUM.length(), file.getLength());
			assertTrue(store.exists(file.getURL()));
			assertTrue(store.exists(file.getId()));

		} finally {
			inputStream.close();
		}

	}

	@Test
	public void testUpload3() throws IOException {
		MongoFileStore store = new MongoFileStore(database, //
				MongoFileStoreConfig.builder()//
						.enableEncryption(new BasicCrypto())//
						.enableCompression(false)//
						.build());
		assertNotNull(store.toString());

		MongoFileUrl url = MongoFileUrl
				.construct("mongofile:/home/oildex/x0064660/invoice/report/activeusers_19.PDF?52fb1e7b36707d6d13ebfda9#application/pdf");

		MongoFile file = new MongoFile(store, url, ChunkSize.small_32K.getChunkSize());

		MongoFileWriterAdapter adapter = new MongoFileWriterAdapter(file);

		FileChunksOutputStreamSink chunks = new FileChunksOutputStreamSink(//
				store.getChunksCollection(), file.getId(), adapter, file.getExpiresAt());

		OutputStream sink = new BufferedChunksOutputStream(chunks, file.getChunkSize());

		sink = new MongoEncryptionOutputStream(store.getConfig(), file, sink);

		sink.write("foo".getBytes());
		sink.write('c');
		sink.flush();
		sink.close();

		System.out.println("Sink = " + sink.toString());
		assertEquals(59, sink.toString().length());
		sink.close();

	}

	@Test(expected = FileNotFoundException.class)
	public void testUpload4() throws IOException {
		MongoFileStore store = new MongoFileStore(database, MongoFileStoreConfig.builder().build());
		assertNotNull(store.toString());

		File file2 = new File("file.does.not.exist");
		store.upload(file2, "text/plain", false, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testException1() throws IOException {

		MongoFileStore store = new MongoFileStore(database, MongoFileStoreConfig.builder().build());
		store.createNew(null, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testException2() throws IOException {

		MongoFileStore store = new MongoFileStore(database, MongoFileStoreConfig.builder().build());
		store.createNew("", null);
	}

	@Test(expected = IllegalStateException.class)
	public void testException3() throws IOException {

		MongoFileStore store = new MongoFileStore(database, MongoFileStoreConfig.builder().enableCompression(false).build());
		store.createNew("", "", null, true);
	}

	@Test(expected = IllegalStateException.class)
	public void testException4() throws IOException {

		MongoFileStore store = new MongoFileStore(database, MongoFileStoreConfig.builder()//
				.enableCompression(false).enableEncryption(new BasicCrypto()).build());
		store.createNew("", "", null, true);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testException5() throws IOException {

		MongoFileStore store = new MongoFileStore(database, MongoFileStoreConfig.builder().build());
		store.upload(null, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testException6() throws IOException {

		MongoFileStore store = new MongoFileStore(database, MongoFileStoreConfig.builder().build());
		store.upload(LoremIpsum.getFile(), null);
	}

	@Test(expected = IllegalStateException.class)
	public void testException8() throws IOException {

		MongoFileStore store = new MongoFileStore(database, MongoFileStoreConfig.builder().enableCompression(false).build());
		store.upload(LoremIpsum.getFile(), "text/plain", true, null);
	}

	@Test(expected = IllegalStateException.class)
	public void testException9() throws IOException {

		MongoFileStore store = new MongoFileStore(database, MongoFileStoreConfig.builder()//
				.enableCompression(false).enableEncryption(new BasicCrypto()).build());
		store.upload(LoremIpsum.getFile(), "text/plain", true, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testException10() throws IOException {

		MongoFileStore store = new MongoFileStore(database, MongoFileStoreConfig.builder().build());
		store.getManifest((MongoFileUrl) null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testException11() throws IOException {

		MongoFileStore store = new MongoFileStore(database, MongoFileStoreConfig.builder().build());
		store.getManifest((MongoFile) null);
	}

	@Test(expected = IllegalStateException.class)
	public void testException12() throws IOException {

		MongoFileStore store = new MongoFileStore(database, MongoFileStoreConfig.builder().build());
		MongoFile file = store.upload(LoremIpsum.getFile(), "text/plain");

		store.getManifest(file);
	}

	@Test(expected = IllegalStateException.class)
	public void testException13() throws IOException {

		MongoFileStore store = new MongoFileStore(database, MongoFileStoreConfig.builder().build());
		MongoFile file = store.upload(LoremIpsum.getFile(), "text/plain");

		store.getManifest(file.getURL());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testException14() throws IOException {

		MongoFileStore store = new MongoFileStore(database, MongoFileStoreConfig.builder().build());
		store.findOne((URL) null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testException15() throws IOException {

		MongoFileStore store = new MongoFileStore(database, MongoFileStoreConfig.builder().build());
		store.findOne((MongoFileUrl) null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testException16() throws IOException {

		MongoFileStore store = new MongoFileStore(database, MongoFileStoreConfig.builder().build());
		store.exists((MongoFileUrl) null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testException17() throws IOException {

		MongoFileStore store = new MongoFileStore(database, MongoFileStoreConfig.builder().build());
		store.createNew("filename", "test/plain").write(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testException18() throws IOException {

		MongoFileStore store = new MongoFileStore(database, MongoFileStoreConfig.builder().build());
		store.remove((Document) null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testException19() throws IOException {

		MongoFileStore store = new MongoFileStore(database, MongoFileStoreConfig.builder().build());
		store.remove((MongoFileUrl) null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testException20() throws IOException {

		MongoFileStore store = new MongoFileStore(database, MongoFileStoreConfig.builder().build());
		store.remove((MongoFile) null);
	}

	@Test(expected = IOException.class)
	public void testException21() throws IOException {

		MongoFileStore store = new MongoFileStore(database, //
				MongoFileStoreConfig.builder().enableEncryption(new BasicCrypto()).build());

		MongoFile file = store.upload(LoremIpsum.getFile().getAbsolutePath(), "text/plain",
				new FileInputStream(LoremIpsum.getFile()));

		OutputStream out = new ThrowIOOnCloseException(new ByteArrayOutputStream());
		store.findOne(file.getURL()).readInto(out, true);
		out.close();
	}

	@Test(expected = RuntimeException.class)
	public void testException22() throws IOException {

		MongoFileStoreConfig config = MongoFileStoreConfig.builder().enableEncryption(new BasicCrypto()).build();
		MongoFileStore store = new MongoFileStore(database, config);

		MongoFile file = store.upload(LoremIpsum.getFile().getAbsolutePath(), "text/plain",
				new FileInputStream(LoremIpsum.getFile()));

		OutputStream out = new ThrowRunOnCloseException(new ByteArrayOutputStream());
		store.findOne(file.getURL()).readInto(out, true);
		out.close();
	}

	class ThrowIOOnCloseException extends FilterOutputStream {

		public ThrowIOOnCloseException(final OutputStream out) {
			super(out);
		}

		@Override
		public void close() throws IOException {
			throw new IOException("test");
		}

	}

	class ThrowRunOnCloseException extends FilterOutputStream {

		public ThrowRunOnCloseException(final OutputStream out) {
			super(out);
		}

		@Override
		public void close() throws IOException {
			throw new RuntimeException("test");
		}

	}
}
