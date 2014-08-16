package org.mongodb;

import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;

public class MongoCollectionOptions {

    WriteConcern writeConcern;
    ReadPreference readPreference;

    public static MongoCollectionOptions builder() {
        return new MongoCollectionOptions();
    }

    public MongoCollectionOptions writeConcern(WriteConcern writeConcern) {
        this.writeConcern = writeConcern;
        return this;
    }

    public MongoCollectionOptions readPreference(ReadPreference readPreference) {
        this.readPreference = readPreference;

        return this;
    }

    public MongoCollectionOptions build() {
        return this;
    }

}
