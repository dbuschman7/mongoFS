package io.buschman.mongoFSPlus.gridFS;

import io.buschman.mongoFSPlus.GridFS;

import com.mongodb.DBCollection;

class CollectionsWrapper {

    public static final DBCollection getChunksCollection(GridFS gridFS) {

        io.buschman.mongoFSPlus.gridFS.GridFS fs = (io.buschman.mongoFSPlus.gridFS.GridFS) gridFS;
        return fs.getChunksCollection();
    }

    public static final DBCollection getFilesCollection(GridFS gridFS) {

        io.buschman.mongoFSPlus.gridFS.GridFS fs = (io.buschman.mongoFSPlus.gridFS.GridFS) gridFS;
        return fs.getFilesCollection();
    }

}
