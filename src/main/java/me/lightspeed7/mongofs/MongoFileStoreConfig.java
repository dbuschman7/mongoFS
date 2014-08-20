package me.lightspeed7.mongofs;

import me.lightspeed7.mongofs.crypto.Crypto;
import me.lightspeed7.mongofs.util.ChunkSize;

import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;

public final class MongoFileStoreConfig {

    public static final ChunkSize DEFAULT_CHUNKSIZE = ChunkSize.medium_256K;

    private String bucket = "fileStore";
    private WriteConcern writeConcern = WriteConcern.JOURNALED;
    private ReadPreference readPreference = ReadPreference.primary();
    private boolean enableCompression = true;
    private ChunkSize chunkSize = DEFAULT_CHUNKSIZE;
    private boolean asyncDeletes = true;
    private Crypto crypto = null;

    private MongoFileStoreConfig() {
        // use Builder
    }

    public String getBucket() {

        return bucket;
    }

    private void setBucket(final String bucket) {

        this.bucket = bucket;
    }

    public WriteConcern getWriteConcern() {

        return writeConcern;
    }

    private void setWriteConcern(final WriteConcern writeConcern) {

        this.writeConcern = writeConcern;
    }

    public ReadPreference getReadPreference() {

        return readPreference;
    }

    private void setReadPreference(final ReadPreference readPreference) {

        this.readPreference = readPreference;
    }

    public boolean isEnableCompression() {

        return enableCompression;
    }

    private void setEnableCompression(final boolean enableCompression) {

        this.enableCompression = enableCompression;
    }

    public ChunkSize getChunkSize() {

        return chunkSize;
    }

    private void setChunkSize(final ChunkSize chunkSize) {

        this.chunkSize = chunkSize;
    }

    public boolean isAsyncDeletes() {

        return asyncDeletes;
    }

    private void setAsyncDeletes(final boolean asyncDeletes) {

        this.asyncDeletes = asyncDeletes;
    }

    public Crypto getCrypto() {
        return crypto;
    }

    public void setCrypto(Crypto crypto) {
        this.crypto = crypto;
    }

    public boolean isCryptoEnabled() {
        return this.crypto != null;
    }

    @Override
    public String toString() {

        return String
                .format("MongoFileStoreConfig [bucket=%s, chunkSize=%s, enableCompression=%s, cryptoEnabled=%s, writeConcern=%s, readPreference=%s]",
                        bucket, chunkSize, enableCompression, crypto != null, writeConcern, readPreference);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private MongoFileStoreConfig config = new MongoFileStoreConfig();

        /**
         * Start a builder
         * 
         * @return the builder
         */
        public MongoFileStoreConfig build() {
            return config;
        }

        /**
         * Enable background deletes for this collection
         * 
         * @param value
         * @return the builder
         */
        public Builder asyncDeletes(final boolean value) {
            config.setAsyncDeletes(value);
            return this;
        }

        /**
         * Set the bucket name for this collection
         * 
         * @param value
         * @return the builder
         */
        public Builder bucket(final String value) {
            if (value == null || value.trim().isEmpty()) {
                throw new IllegalArgumentException("bucket name cannot be nul of empty");
            }
            config.setBucket(value);
            return this;
        }

        /**
         * Specifies the chunk size to use for data chunks. The size here cause buffers on the chunkSize to kept inside the writing and
         * reading processes. So be advised on using memory wisely, large chunksize means larger buffers internally.
         * 
         * @param chunkSize
         * @return the builder
         */
        public Builder chunkSize(final ChunkSize chunkSize) {
            config.setChunkSize(chunkSize);
            return this;
        }

        /**
         * Enable compression on this collection
         * 
         * NOTE: Cannot be used with encryption enabled as well.
         * 
         * @param value
         * @return the builder
         */
        public Builder enableCompression(final boolean value) {
            if (value == true && config.crypto != null) {
                throw new IllegalStateException("Compression and Encryption cannot be enabled at the same time");
            }
            config.setEnableCompression(value);
            return this;
        }

        /**
         * Enable encryption on this collection
         * 
         * NOTE: Cannot be used with compression enabled as well.
         * 
         * @param crypto
         * @return the builder
         */

        public Builder enableEncryption(final Crypto crypto) {
            if (config.enableCompression == true && crypto != null) {
                throw new IllegalStateException("Compression and Encryption cannot be enabled at the same time");
            }

            if (crypto == null) {
                return this;
            }

            if (crypto.getChunkSize() == null) {
                throw new IllegalArgumentException("Encryption algorithm must specfic chunk size");
            }

            if (crypto.getChunkSize().greaterThan(config.getChunkSize())) {
                throw new IllegalArgumentException("Encryption chunk size cannot be greater than file chunk size ");
            }

            if (crypto.getChunkSize() == ChunkSize.mongo_16M) {
                throw new IllegalArgumentException(
                        "Encryption chunk size cannot be be 'mongo_16M', since that is the max size for MongoDB documents and excrypting may increase the size of the data to be saved in a single chunk");
            }

            config.setCrypto(crypto);
            return this;
        }

        /**
         * Set the readPreference on the collection
         * 
         * @param value
         * @return the builder
         */
        public Builder readPreference(final ReadPreference value) {
            config.setReadPreference(value);
            return this;
        }

        /**
         * Set the WriteConcern for this collection
         * 
         * @param value
         * @return the builder
         */
        public Builder writeConcern(final WriteConcern value) {
            config.setWriteConcern(value);
            return this;
        }
    }

}
