package me.lightspeed7.mongofs.common;

import java.util.Set;
import java.util.TreeSet;

/**
 * 
 * @author David Buschman
 * 
 */
public enum MongoFileConstants {

    _id(true),
    filename(true),
    contentType(true),
    chunkSize(true),
    length(true),
    uploadDate(true),
    aliases(true),
    md5(true),
    chunkCount(false),
    compressedLength(false),
    compressionRatio(false),
    compressionFormat(false)

    //
    ;

    private boolean core = false;

    private MongoFileConstants(boolean core) {

        this.core = core;
    }

    public static Set<String> getCoreFields(boolean mongoFS) {

        Set<String> set = new TreeSet<>();

        for (MongoFileConstants current : MongoFileConstants.values()) {
            if (current.core || mongoFS) {
                set.add(current.name());
            }
        }

        return set;
    }

}
