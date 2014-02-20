package io.buschman.mongoFSPlus.legacy;

import io.buschman.mongoFSPlus.GridFS;

import com.mongodb.DBCollection;

class CollectionsWrapper {

    public static final DBCollection getChunksCollection(GridFS gridFS) {

        io.buschman.mongoFSPlus.legacy.GridFS fs = (io.buschman.mongoFSPlus.legacy.GridFS) gridFS;
        return fs.getChunksCollection();
    }

    public static final DBCollection getFilesCollection(GridFS gridFS) {

        io.buschman.mongoFSPlus.legacy.GridFS fs = (io.buschman.mongoFSPlus.legacy.GridFS) gridFS;
        return fs.getFilesCollection();
    }

}
