package me.lightspeed7.mongofs;

import java.util.Set;
import java.util.TreeSet;

/**
 * 
 * @author David Buschman
 * 
 */
public enum MongoFileConstants {

    _id(true), //
    filename(true), //
    contentType(true), //
    chunkSize(true), //
    length(true), //
    uploadDate(true), //
    aliases(true), //
    md5(true), //
    metadata(true), //
    chunkCount(false), //
    compressedLength(false), // deprecated, now uses storage
    storage(false), //
    compressionRatio(false), // deprecated, now uses ratio
    ratio(false), //
    compressionFormat(false), // deprecated, now uses format
    format(false), //
    expireAt(false), //
    deleted(false), //
    manifestId(false), // expanded zip file support
    manifestNum(false); // expanded zip file support
    //

    private boolean core = false;

    private MongoFileConstants(final boolean core) {

        this.core = core;
    }

    public static Set<String> getFields(final boolean mongoFS) {

        Set<String> set = new TreeSet<String>();

        for (MongoFileConstants current : MongoFileConstants.values()) {
            if (current.core || mongoFS) {
                set.add(current.name());
            }
        }

        return set;
    }

}
