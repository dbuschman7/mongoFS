package org.mongodb;

import com.mongodb.DB;
import com.mongodb.DBCollection;

public class MongoDatabase {

    private DB surrogate;

    public MongoDatabase(final DB surrogate) {
        this.setSurrogate(surrogate);

    }

    public CommandResult executeCommand(final Document cmd) {

        CommandResult result = new CommandResult(getSurrogate().command(cmd.getSurrogate()));
        return result;
    }

    public Object getName() {
        return getSurrogate().getName();
    }

    public MongoCollection<Document> getCollection(final String name, final MongoCollectionOptions options) {
        DBCollection collection = getSurrogate().getCollection(name);
        collection.setReadPreference(options.getReadPreference());
        collection.setWriteConcern(options.getWriteConcern());
        return new MongoCollection<Document>(collection);
    }

    public DB getSurrogate() {
        return surrogate;
    }

    public void setSurrogate(final DB surrogate) {
        this.surrogate = surrogate;
    }

}
