package me.lightspeed7.mongofs;

import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;

public class MongoFileStoreConfig {

    private String bucket = "mongofs";
    private WriteConcern writeConcern = WriteConcern.NORMAL;
    private ReadPreference readPreference = ReadPreference.primary();
    private boolean enableCompression = true;
    private int chunkSize = MongoFileStore.DEFAULT_CHUNKSIZE;

    public MongoFileStoreConfig(String bucket) {

        this.bucket = bucket;
    }

    public String getBucket() {

        return bucket;
    }

    public void setBucket(String bucket) {

        this.bucket = bucket;
    }

    public WriteConcern getWriteConcern() {

        return writeConcern;
    }

    public void setWriteConcern(WriteConcern writeConcern) {

        this.writeConcern = writeConcern;
    }

    public ReadPreference getReadPreference() {

        return readPreference;
    }

    public void setReadPreference(ReadPreference readPreference) {

        this.readPreference = readPreference;
    }

    public boolean isEnableCompression() {

        return enableCompression;
    }

    public void setEnableCompression(boolean enableCompression) {

        this.enableCompression = enableCompression;
    }

    public int getChunkSize() {

        return chunkSize;
    }

    public void setChunkSize(int chunkSize) {

        this.chunkSize = chunkSize;
    }

    @Override
    public String toString() {

        return String
                .format("MongoFileStoreConfig [bucket=%s, chunkSize=%s, enableCompression=%s, writeConcern=%s, readPreference=%s]",
                        bucket, chunkSize, enableCompression, writeConcern, readPreference);
    }

}
