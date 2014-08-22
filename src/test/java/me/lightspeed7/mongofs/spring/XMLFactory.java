package me.lightspeed7.mongofs.spring;

import me.lightspeed7.mongofs.MongoFileStore;

public class XMLFactory {

    public static final MongoFileStore initialize() {

        JavaConfig javaConfig = new JavaConfig();

        return new MongoFileStore(javaConfig.client().getDB(JavaConfig.COLLECTION_NAME), javaConfig.configure());
    }
}
