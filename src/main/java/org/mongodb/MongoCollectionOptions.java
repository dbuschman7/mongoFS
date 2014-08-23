package org.mongodb;

import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;

public class MongoCollectionOptions {

    private WriteConcern writeConcern;
    private ReadPreference readPreference;

    public static MongoCollectionOptions builder() {
        return new MongoCollectionOptions();
    }

    public MongoCollectionOptions writeConcern(final WriteConcern writeConcern) {
        this.setWriteConcern(writeConcern);
        return this;
    }

    public MongoCollectionOptions readPreference(final ReadPreference readPreference) {
        this.setReadPreference(readPreference);

        return this;
    }

    public MongoCollectionOptions build() {
        return this;
    }

    public WriteConcern getWriteConcern() {
        return writeConcern;
    }

    public void setWriteConcern(final WriteConcern writeConcern) {
        this.writeConcern = writeConcern;
    }

    public ReadPreference getReadPreference() {
        return readPreference;
    }

    public void setReadPreference(final ReadPreference readPreference) {
        this.readPreference = readPreference;
    }

}
