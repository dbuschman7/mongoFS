package org.mongodb;

import com.mongodb.DB;
import com.mongodb.DBCollection;

public class MongoDatabase {

    public DB surrogate;

    public MongoDatabase(DB surrogate) {
        this.surrogate = surrogate;

    }

    public CommandResult executeCommand(Document cmd) {

        CommandResult result = new CommandResult(surrogate.command(cmd.surrogate));
        return result;
    }

    public Object getName() {
        return surrogate.getName();
    }

    public MongoCollection<Document> getCollection(String name, MongoCollectionOptions options) {
        DBCollection collection = surrogate.getCollection(name);
        collection.setReadPreference(options.readPreference);
        collection.setWriteConcern(options.writeConcern);
        return new MongoCollection<Document>(collection);
    }

}
