package me.lightspeed7.mongofs;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.zip.GZIPInputStream;

import me.lightspeed7.mongofs.url.MongoFileUrl;
import me.lightspeed7.mongofs.url.Parser;
import me.lightspeed7.mongofs.url.StorageFormat;
import me.lightspeed7.mongofs.util.BytesCopier;

import org.bson.types.ObjectId;
import org.mongodb.Document;
import org.mongodb.MongoException;

import com.mongodb.BasicDBObject;

/**
 * Object to hold the state of the file's metatdata. To persist this outside of
 * MongoFS, use the getURL() and persist that.
 * 
 * @author David Buschman
 * 
 */
public class MongoFile implements InputFile {

	private final Document surrogate;

	private final MongoFileStore store;

	private StorageFormat format;

	/**
	 * Construct a MongoFile object for reading data
	 * 
	 * @param store
	 * @param o
	 */
	/* package */MongoFile(final MongoFileStore store, final Document surrogate) {

		this.store = store;
		this.surrogate = surrogate;
		this.format = fetchFormat(surrogate);
	}

	private StorageFormat fetchFormat(final Document surrogate) {
		String format = surrogate.getString(MongoFileConstants.format);
		if (format == null) {
			format = surrogate.getString(MongoFileConstants.compressionFormat);
		}
		if (format == null) {
			return StorageFormat.GRIDFS;
		}
		return StorageFormat.find(format);
	}

	/**
	 * Construct a MongoFile object for writing data
	 * 
	 * @param collection
	 * @param url
	 */
	/* package */MongoFile(final MongoFileStore store, final MongoFileUrl url,
			final long chunkSize) {

		this.store = store;
		this.format = url.getFormat();

		surrogate = new Document();
		surrogate.put(MongoFileConstants._id.toString(), url.getMongoFileId());
		surrogate.put(MongoFileConstants.uploadDate.toString(), new Date());

		surrogate.put(MongoFileConstants.chunkSize.toString(), chunkSize);
		surrogate
				.put(MongoFileConstants.filename.toString(), url.getFilePath());
		surrogate.put(MongoFileConstants.contentType.toString(),
				url.getMediaType());
		if (url.getFormat() != null) {
			surrogate.put(MongoFileConstants.format.toString(), url.getFormat()
					.getCode());
		}
	}

	//
	// logic methods
	// //////////////////
	private String getBucketName() {

		return store.getFilesCollection().getName().split("\\.")[0];
	}

	/**
	 * Saves the file entry meta data to the files collection
	 * 
	 * @throws MongoException
	 */
	public void save() {
		System.out.println("MongoFile - save surrogate");

		store.getFilesCollection().save(surrogate);
	}

	/**
	 * Verifies that the MD5 matches between the database and the local file.
	 * This should be called after transferring a file.
	 * 
	 * @throws MongoException
	 */
	public void validate() {

		MongoFileConstants md5key = MongoFileConstants.md5;
		String md5 = getString(md5key);
		if (md5 == null) {
			throw new MongoException("no md5 stored");
		}

		Document cmd = new Document("filemd5", get(MongoFileConstants._id));
		cmd.put("root", getBucketName());
		Document res = store.getFilesCollection().getDatabase()
				.executeCommand(cmd).getResponse();
		if (res != null && res.containsKey(md5key.toString())) {
			String m = res.get(md5key.toString()).toString();
			if (m.equals(md5)) {
				return;
			}
			throw new MongoException("md5 differ.  mine [" + md5 + "] theirs ["
					+ m + "]");
		}

		// no md5 from the server
		throw new MongoException("no md5 returned from server: " + res);

	}

	public MongoFileUrl getURL() throws MalformedURLException {

		// compression and encrypted read from stored format
		URL url = Parser.construct(getId(), getFilename(), getContentType(),
				this.format);
		return MongoFileUrl.construct(url);
	}

	/**
	 * Return an input stream to read the file content data from
	 * 
	 * @return an input stream to read from
	 * 
	 * @throws IOException
	 */
	public final InputStream getInputStream() throws IOException {

		// /////////////////////////////////////////////////////////
		// Assembled backwards
		//
		// Plain Text -- returned <- counting <- chunks
		// Compressed -- returned <- gzip <- counting <- chunks
		// Encryption -- returned <- decrypt <- counting <- chunks
		// Enc + Comp -- returned <- gzip <- decrypt <- counting <- chunks
		//
		// /////////////////////////////////////////////////////////
		InputStream returned = new FileChunksInputStreamSource(store, this);
		returned = new CountingInputStream(this, returned);

		if (getURL().isStoredEncrypted()) {

			if (store.getConfig().getEncryption() == null) {
				throw new IllegalStateException(
						"File is stored in ecrypted but store is not configured for decryption");
			}

			returned = new DecryptInputStream(
					store.getConfig().getEncryption(), this.getStorageLength(),
					returned);
		}

		if (getURL().isStoredCompressed()) {
			returned = new GZIPInputStream(returned);
		}

		return returned;
	}

	/**
	 * Copy the content to the given output stream
	 * 
	 * @param out
	 *            the output stream to write to
	 * 
	 * @param flush
	 *            should the output stream be flush when all the data has been
	 *            written.
	 * 
	 * @throws IOException
	 */
	public OutputStream readInto(final OutputStream out, final boolean flush)
			throws IOException {

		new BytesCopier(getInputStream(), out).transfer(flush);

		return out;
	}

	/**
	 * Read the contents on the file into a String
	 * 
	 * NOTE : This uses heap memory to store the contents on the file, the user
	 * is responsible to know that the file can safely fit into the memory space
	 * of the running application. The memory allocated is chunkSize *
	 * chunkCount
	 * 
	 * @return the file contents as a string
	 * @throws IOException
	 */
	public String readIntoString() throws IOException {

		ByteArrayOutputStream out = new ByteArrayOutputStream(
				this.getChunkSize() * this.getChunkCount());
		new BytesCopier(getInputStream(), out).transfer(true);
		return out.toString("UTF-8");
	}

	//
	// read-only fields
	// ///////////////////////

	/**
	 * Returns the number of chunks that store the file data.
	 * 
	 * @return number of chunks
	 */
	public int getChunkCount() {

		// for compatibility with legacy GridFS implementations, if -1 comes
		// back, then legacy file
		int chunkCount = getInt(MongoFileConstants.chunkCount, -1);
		if (chunkCount == -1) {
			chunkCount = (int) Math.ceil((double) getLength() / getChunkSize());
		}
		return chunkCount;
	}

	/**
	 * Gets the file's length.
	 * 
	 * @return the length of the file
	 */
	public long getLength() {

		return getLong(MongoFileConstants.length);
	}

	/**
	 * Gets the file's length on MongoDB, this is the actual size stored.
	 * 
	 * NOTE: For compressed and encrypted files, this size will differ from the
	 * getLength() method.
	 * 
	 * @return the length of the file
	 */
	public long getStorageLength() {

		if (containsKey(MongoFileConstants.storage.name())) {
			return getLong(MongoFileConstants.storage);
		} else { // deprecated, files stored from 0.7.x versions and before
			return getLong(MongoFileConstants.compressedLength);
		}
	}

	/**
	 * Gets the observed MD5 during transfer
	 * 
	 * @return md5
	 */
	public String getMD5() {

		return getString(MongoFileConstants.md5);
	}

	/**
	 * Gets the id.
	 * 
	 * @return the id of the file.
	 */
	public ObjectId getId() {

		return (ObjectId) surrogate.get("_id");
	}

	/**
	 * Gets the filename.
	 * 
	 * @return the name of the file
	 */
	public String getFilename() {

		return getString(MongoFileConstants.filename);
	}

	/**
	 * Gets the content type.
	 * 
	 * @return the content type
	 */
	public String getContentType() {

		return getString(MongoFileConstants.contentType);
	}

	/**
	 * Gets the size of a chunk.
	 * 
	 * @return the chunkSize
	 */
	public int getChunkSize() {

		return getInt(MongoFileConstants.chunkSize);
	}

	/**
	 * Gets the upload date.
	 * 
	 * @return the date
	 */
	public Date getUploadDate() {

		return (Date) get(MongoFileConstants.uploadDate);
	}

	/**
	 * Has the file been marked deleted but not yet removed from the store
	 * 
	 * @return true is the file is scheduled for deletion
	 */
	public boolean isDeleted() {

		return getBoolean(MongoFileConstants.deleted, false);
	}

	//
	// getters and setters
	// /////////////////////////

	/**
	 * Gets the aliases from the metadata. note: to set aliases, call put(
	 * "aliases" , List<String> )
	 * 
	 * @return list of aliases
	 */
	@SuppressWarnings("unchecked")
	public List<String> getAliases() {

		return (List<String>) get(MongoFileConstants.aliases);
	}

	public void setAliases(final List<String> aliases) {

		put(MongoFileConstants.aliases, aliases);
	}

	/**
	 * Gets the file metadata.
	 * 
	 * @return the metadata
	 */
	public Document getMetaData() {

		Object object = get(MongoFileConstants.metadata);
		if (object == null) {
			return null;
		}

		return new Document((BasicDBObject) object);
	}

	/**
	 * Gets the file metadata.
	 * 
	 * @param metadata
	 *            metadata to be set
	 */
	public void setMetaData(final Document metadata) {

		put(MongoFileConstants.metadata, metadata);
	}

	/**
	 * Add an object to the metadata subclass
	 * 
	 * @param key
	 * @param value
	 */
	public Object setInMetaData(final String key, final Object value) {

		Document object = getMetaData();
		if (object == null) {
			object = new Document();
			setMetaData(object);
		}
		return object.put(key, value);
	}

	/**
	 * Internal use only
	 * 
	 * @param when
	 */
	/* package */void setExpiresAt(final Date when) {

		put(MongoFileConstants.expireAt, when);
	}

	/**
	 * Return the expiration date for this file.
	 * 
	 * @return the expiration date for the file, null if is has none
	 */
	public Date getExpiresAt() {

		return getDate(MongoFileConstants.expireAt, null);
	}

	//
	// helpers
	// /////////////////

	/**
	 * Put a value into the object for a given key
	 * 
	 * @param key
	 * @param value
	 * @return the previous value
	 */
	public Object put(final String key, final Object value) {

		if (key == null) {
			throw new IllegalArgumentException("key should never be null");
		}

		return surrogate.put(key, value);
	}

	/**
	 * Set a value based on the defined MongoFileConstants
	 * 
	 * @param key
	 * @param value
	 * @return the current value, or the default if its null
	 */
	public Object put(final MongoFileConstants key, final Object value) {

		if (key == null) {
			throw new IllegalArgumentException("key should never be null");
		}

		return surrogate.put(key.name(), value);
	}

	/**
	 * Get an the value on any attribute in the system
	 * 
	 * @param key
	 * @return the current value
	 */
	public Object get(final String key) {

		if (key == null) {
			throw new IllegalArgumentException("Key should never be null");
		}
		return surrogate.get(key);
	}

	/**
	 * Get an the value on any attribute in the system
	 * 
	 * @param key
	 * @return the current value
	 */
	public Object get(final MongoFileConstants key) {

		if (key == null) {
			throw new IllegalArgumentException("Key should never be null");
		}
		return surrogate.get(key.name());
	}

	/**
	 * Return the value for the given key as a string
	 * 
	 * @param key
	 * 
	 * @return the string value
	 */
	public String getString(final MongoFileConstants key) {

		return (String) surrogate.get(key.name());
	}

	/**
	 * Return the value for the given key as a integer
	 * 
	 * @param key
	 * 
	 * @return the key's value as an integer
	 */
	public int getInt(final MongoFileConstants key) {
		return getInt(key, -1);
	}

	/**
	 * Return the value for the given key as a long
	 * 
	 * @param key
	 * 
	 * @return the value as a long
	 */
	public long getLong(final MongoFileConstants key) {
		return getLong(key, -1);
	}

	/**
	 * Returns the value of a field as an <code>int</code>.
	 * 
	 * @param key
	 *            the field to look for
	 * @param def
	 *            the default to return
	 * @return the field value (or default)
	 */
	public int getInt(final MongoFileConstants key, final int def) {

		if (key == null) {
			throw new IllegalArgumentException("key cannot be null");
		}

		Object value = surrogate.get(key.name());
		if (value == null) {
			return def;
		}

		return Integer.parseInt(value.toString());
	}

	/**
	 * Returns the value of a field as an <code>long</code>.
	 * 
	 * @param key
	 *            the field to look for
	 * @param def
	 *            the default to return
	 * @return the field value (or default)
	 */
	public long getLong(final MongoFileConstants key, final long def) {
		if (key == null) {
			throw new IllegalArgumentException("key cannot be null");
		}

		Object value = surrogate.get(key.name());
		if (value == null) {
			return def;
		}

		return Long.parseLong(value.toString());
	}

	/**
	 * Returns the value of a field as an <code>double</code>.
	 * 
	 * @param key
	 *            the field to look for
	 * @param def
	 *            the default to return
	 * @return the field value (or default)
	 */
	public double getDouble(final MongoFileConstants key, final double def) {

		if (key == null) {
			throw new IllegalArgumentException("key cannot be null");
		}

		Object value = surrogate.get(key.name());
		if (value == null) {
			return def;
		}

		return Double.parseDouble(value.toString());
	}

	/**
	 * Returns the value of a field as a string
	 * 
	 * @param key
	 *            the field to look up
	 * @param def
	 *            the default to return
	 * @return the value of the field, converted to a string
	 */
	public String getString(final MongoFileConstants key, final String def) {

		if (key == null) {
			throw new IllegalArgumentException("key cannot be null");
		}

		Object value = surrogate.get(key.name());
		if (value == null) {
			return def;
		}

		return value.toString();
	}

	/**
	 * Returns the value of a field as a boolean
	 * 
	 * @param key
	 *            the field to look up
	 * @param def
	 *            the default value in case the field is not found
	 * @return the value of the field, converted to a string
	 */
	public boolean getBoolean(final MongoFileConstants key, final boolean def) {

		Object foo = surrogate.get(key.name());
		if (foo == null) {
			return def;
		}
		if (foo instanceof Number) {
			return ((Number) foo).intValue() > 0;
		}
		if (foo instanceof Boolean) {
			return ((Boolean) foo).booleanValue();
		}
		throw new IllegalArgumentException("can't coerce to bool:"
				+ foo.getClass());
	}

	/**
	 * Returns the object id or def if not set.
	 * 
	 * @param key
	 *            The field to return
	 * @param def
	 *            the default value in case the field is not found
	 * @return The field object value or def if not set.
	 */
	public ObjectId getObjectId(final MongoFileConstants key, final ObjectId def) {

		final Object foo = surrogate.get(key.toString());
		return (foo != null) ? (ObjectId) foo : def;
	}

	/**
	 * Returns the date or def if not set.
	 * 
	 * @param key
	 *            The key to return
	 * @param def
	 *            the default value in case the field is not found
	 * @return The field object value or def if not set.
	 */
	public Date getDate(final MongoFileConstants key, final Date def) {

		final Object foo = surrogate.get(key.toString());
		return (foo != null) ? (Date) foo : def;
	}

	/**
	 * Does a key exist in the object
	 * 
	 * @param key
	 * 
	 * @return true if it exists
	 */
	public boolean containsKey(final String key) {

		return this.surrogate.containsKey(key);
	}

	// private Set<String> keySet() {
	//
	// Set<String> keys = new HashSet<String>();
	// keys.addAll(MongoFileConstants.getFields(true));
	// keys.addAll(surrogate.keySet());
	// return keys;
	// }

	@Override
	public String toString() {

		return surrogate.toString();
	}

	public boolean isCompressed() {

		return this.format.isCompressed();
	}

	public boolean isEncrypted() {
		return this.format.isEncrypted();
	}

	public boolean isExpandedZipFile() {

		if (0 == this.getInt(MongoFileConstants.manifestNum, -1)) {
			return true;
		}
		return false;
	}

}
