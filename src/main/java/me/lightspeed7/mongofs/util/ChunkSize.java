package me.lightspeed7.mongofs.util;

public enum ChunkSize {

    /**
     * lots of small files only
     */
    tiny_4K(4),

    /**
     * lots of small files only
     */
    tiny_8K(8),

    /**
     * still small files mostly
     */
    small_32K(32),

    /**
     * still small files mostly
     */
    small_64K(64),

    /**
     * good compromise, NOTE: this is the default
     */
    medium_256K(256),

    /**
     * good compromise, mixed files
     */
    medium_512K(512),

    /**
     * for lots of larger files
     */
    large_1M(1024),

    /**
     * for lots of larger files
     */
    large_2M(2 * 1024),

    /**
     * mega files only, no small files
     */
    huge_4M(4 * 1024),

    /**
     * mega files only, no small files
     */
    huge_8M(8 * 1024),

    /**
     * the largest file chunk size currently possible, Mongo documents size limit
     */
    mongo_16M(16 * 1024);
    //

    private int k;

    private ChunkSize(final int k) {

        this.k = k;
    }

    public int getChunkSize() {

        return (this.k * 1024) - BREATHING_ROOM;
    }

    public boolean greaterThan(final ChunkSize other) {
        return this.k > other.k;
    }

    // number of bytes to give as breathing room for other parts of the JSON
    // document in the chunks collection
    // and 512 offset for power of 2 sizing
    private static final int BREATHING_ROOM = 512 + 200;

}
