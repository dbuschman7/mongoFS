package me.lightspeed7.mongofs.url;

import java.net.MalformedURLException;
import java.net.URL;

import me.lightspeed7.mongofs.util.CompressionMediaTypes;

import org.bson.types.ObjectId;

public final class Parser {

	private Parser() {
		// hidden
	}

	public static URL construct(final ObjectId id, final String fileName, final String mediaType, final StorageFormat format)
			throws MalformedURLException {

		String protocol = MongoFileUrl.PROTOCOL;

		boolean compressed = format.isCompressed() && CompressionMediaTypes.isCompressable(mediaType);
		if (compressed && format.isEncrypted()) {
			protocol += ":" + StorageFormat.ENCRYPTED_GZIP.getCode();
		} else if (compressed) {
			protocol += ":" + StorageFormat.GZIPPED.getCode();
		} else if (format.isEncrypted()) {
			protocol += ":" + StorageFormat.ENCRYPTED.getCode();
		}

		return construct(String.format("%s:%s?%s#%s", protocol, fileName, id.toString(), mediaType == null ? "" : mediaType.toString()));
	}

	public static URL construct(final String spec) throws MalformedURLException {

		return new URL(null, spec, new Handler());
	}

}
