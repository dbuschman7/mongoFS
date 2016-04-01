package me.lightspeed7.mongofs;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

public final class MongoTestConfig {

	public static MongoClient construct() {
		return new MongoClient(new MongoClientURI("mongodb://services.local:27017"));
	}

	private MongoTestConfig() {
		// empty
	}
}
