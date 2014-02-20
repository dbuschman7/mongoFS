package io.buschman.mongoFSPlus;

import com.mongodb.DB;

public class MongoFSFactory {

    public static final GridFS constructGridFS(DB database) {

        return constructGridFS(database, GridFS.DEFAULT_BUCKET);
    }

    public static final GridFS constructGridFS(DB database, String bucket) {

        return new io.buschman.mongoFSPlus.legacy.GridFS(database, bucket);
    }

}
