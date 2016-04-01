package me.lightspeed7.mongofs;

import me.lightspeed7.mongofs.url.MongoFileUrl;

import org.mongodb.Document;
import org.mongodb.MongoCursor;

public final class FileStoreHelper {

    private FileStoreHelper() {
        // empty
    }

    public static MongoFile internalFind(final MongoFileStore store, final MongoFileUrl url) {

        MongoCursor<Document> cursor = store.getFilesCollection().find(//
                new Document().append(MongoFileConstants._id.toString(), url.getMongoFileId())).get();

        return cursor.hasNext() ? new MongoFile(store, cursor.next()) : null;
    }
}
