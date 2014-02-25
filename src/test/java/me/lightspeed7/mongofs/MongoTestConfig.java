package me.lightspeed7.mongofs;

import java.net.UnknownHostException;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

public class MongoTestConfig {

    public static final MongoClient constructMongoClient() {

        MongoClientURI mongoURI = new MongoClientURI("mongodb://cayman-vm:27017");
        try {
            return new MongoClient(mongoURI);
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Invalid Mongo URI: " + mongoURI.getURI(), e);
        }

    }
}
