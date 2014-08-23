package me.lightspeed7.mongofs;

import java.net.UnknownHostException;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

public class MongoTestConfig {

    public static final MongoClient construct() {

        try {
            return new MongoClient(new MongoClientURI("mongodb://cayman-vm:27017")); // my vm server
        } catch (UnknownHostException e) {
            // System.out.println("Cayman-vm unavailabel, trying localhost");
            try {
                return new MongoClient(new MongoClientURI("mongodb://localhost:27017")); // most others
            } catch (UnknownHostException ex) {
                throw new IllegalArgumentException("Unable to connect a mongoDB instance", ex);
            }
        }
    }

    private MongoTestConfig() {
        // empty
    }
}
