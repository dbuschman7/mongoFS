package org.mongodb;

import java.util.ArrayList;
import java.util.List;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

public class CollectionAdministration {

    private DBCollection surrogate;

    public CollectionAdministration(DBCollection surrogate) {
        this.surrogate = surrogate;
    }

    public List<Document> getIndexes() {
        List<Document> indexes = new ArrayList<Document>();

        for (DBObject dbObject : surrogate.getIndexInfo()) {
            indexes.add(new Document(dbObject));
        }
        return indexes;
    }

    public void createIndexes(List<Index> indexes) {
        for (Index index : indexes) {

            // keys
            BasicDBObject keys = new BasicDBObject();
            Document document = index.getKeys();
            for (String key : document.keySet()) {
                keys.put(key, document.get(key));
            }

            // options
            BasicDBObject options = new BasicDBObject();
            options.put("name", index.getName());
            if (index.isUnique()) {
                options.put("unique", index.isUnique());
            }
            if (index.isSparse()) {
                options.put("sparse", index.isSparse());
            }
            if (index.isDropDups()) {
                options.put("dropDups", index.isDropDups());
            }
            if (index.isBackground()) {
                options.put("background", index.isBackground());
            }
            if (index.getExpireAfterSeconds() != -1) {
                options.put("expireAfterSeconds", index.getExpireAfterSeconds());
            }

            // extras not supported yet, this is an adapter after all

            surrogate.createIndex(keys, options);
        }
    }
}
