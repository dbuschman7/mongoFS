package me.lightspeed7.mongofs;

import me.lightspeed7.mongofs.url.MongoFileUrl;

import org.mongodb.Document;

public class MongoZipArchiveQuery {

    private MongoFileStore store;
    private MongoFileUrl zipFileUrl;

    /* package */MongoZipArchiveQuery(final MongoFileStore store, final MongoFileUrl zipFileUrl) {
        this.store = store;
        this.zipFileUrl = zipFileUrl;
    }

    /**
     * finds a list of files matching the given filename
     * 
     * @param filename
     * @return the MongoFileCursor object
     * @throws MongoException
     */
    public MongoFileCursor find(final String filename) {
        return find(filename, null);
    }

    /**
     * finds a list of files matching the given filename
     * 
     * @param filename
     * @param sort
     * @return the MongoFileCursor object
     * @throws MongoException
     */
    public MongoFileCursor find(final String filename, final Document sort) {
        return find(new Document(MongoFileConstants.filename.toString(), filename), sort);

    }

    /**
     * finds a list of files matching the given query
     * 
     * @param query
     * @return the MongoFileCursor object
     * @throws MongoException
     */
    public MongoFileCursor find(final Document query) {
        return find(query, null);
    }

    /**
     * finds a list of files matching the given query
     * 
     * @param query
     * @param sort
     * @return the MongoFileCursor object
     * @throws MongoException
     */
    public MongoFileCursor find(final Document query, final Document sort) {

        return store.find(query.append(MongoFileConstants.manifestId.name(), zipFileUrl.getMongoFileId()), sort);
    }
}
