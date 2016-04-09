package me.lightspeed7.mongofs.url;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.URL;

import org.bson.types.ObjectId;
import org.junit.Test;

public class ParserTest {

	public static final String PDF = "application/pdf";
	public static final String ZIP = "application/zip";

	@Test
	public void testAutoAssignedCompression() throws IOException {

		ObjectId id = new ObjectId();
		URL url = Parser.construct(id, "fileName.pdf", PDF, StorageFormat.GZIPPED);

		assertNotNull(url);
		assertEquals(String.format("mongofile:gz:fileName.pdf?%s#application/pdf", id.toString()), url.toString());

	}

	@Test
	public void testNoCompression() throws IOException {

		ObjectId id = new ObjectId();
		URL url = Parser.construct(id, "fileName.zip", ZIP, StorageFormat.GRIDFS);

		assertNotNull(url);
		assertEquals(String.format("mongofile:fileName.zip?%s#application/zip", id.toString()), url.toString());

	}

	@Test
	public void testBlockedCompression() throws IOException {

		ObjectId id = new ObjectId();
		URL url = Parser.construct(id, "fileName.pdf", PDF, StorageFormat.GRIDFS);

		assertNotNull(url);
		assertEquals(String.format("mongofile:fileName.pdf?%s#application/pdf", id.toString()), url.toString());

	}

	@Test
	public void testAssignedEncryption() throws IOException {

		ObjectId id = new ObjectId();
		URL url = Parser.construct(id, "fileName.pdf", PDF, StorageFormat.ENCRYPTED);

		assertNotNull(url);
		assertEquals(String.format("mongofile:enc:fileName.pdf?%s#application/pdf", id.toString()), url.toString());
	}

	@Test
	public void testAssignedCompressionAndEncryption() throws IOException {

		ObjectId id = new ObjectId();
		URL url = Parser.construct(id, "fileName.pdf", PDF, StorageFormat.ENCRYPTED_GZIP);

		assertNotNull(url);
		assertEquals(String.format("mongofile:encgz:fileName.pdf?%s#application/pdf", id.toString()), url.toString());

	}
}
