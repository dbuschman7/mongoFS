package me.lightspeed7.mongofs.spring;

import me.lightspeed7.mongofs.MongoFileStore;

public final class XMLFactory {

    public static MongoFileStore initialize() {

        JavaConfig javaConfig = new JavaConfig();

        return new MongoFileStore(javaConfig.client().getDB(JavaConfig.COLLECTION_NAME), javaConfig.configure());
    }

    private XMLFactory() {
        // empty
    }
}
