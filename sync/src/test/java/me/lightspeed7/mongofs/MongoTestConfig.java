package me.lightspeed7.mongofs;

import com.google.common.collect.Lists;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

public final class MongoTestConfig {

	public static MongoClient construct() {
		MongoCredential cred = MongoCredential.createScramSha1Credential("rootAdmin", "admin", "password".toCharArray());
		ServerAddress address = new ServerAddress("services.local");
		return new MongoClient(Lists.newArrayList(address), Lists.newArrayList(cred));
	}

	private MongoTestConfig() {
		// empty
	}
}
