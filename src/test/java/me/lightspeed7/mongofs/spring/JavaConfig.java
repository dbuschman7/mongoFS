package me.lightspeed7.mongofs.spring;

import me.lightspeed7.mongofs.MongoFileStore;
import me.lightspeed7.mongofs.MongoFileStoreConfig;
import me.lightspeed7.mongofs.util.ChunkSize;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;

@Configuration
@ComponentScan("me.lightspeed7.mongofs.spring")
public class JavaConfig {

	static final String COLLECTION_NAME = "MongoFS-JavaConfig";

	@Bean(name = "mongoClient")
	public MongoClient client() {
		return new MongoClient(new MongoClientURI("mongodb://services.local:27017"));
	}

	public MongoFileStoreConfig configure() {

		MongoFileStoreConfig config = MongoFileStoreConfig.builder().bucket("spring") //
				.asyncDeletes(true) // background deleting
				.chunkSize(ChunkSize.medium_256K) // good default
				.enableCompression(true)//
				.readPreference(ReadPreference.secondaryPreferred())//
				.writeConcern(WriteConcern.ACKNOWLEDGED)//
				.build();

		return config;
	}

	@Bean(name = "mongoFileStore")
	public MongoFileStore store() {

		return new MongoFileStore(client().getDB(COLLECTION_NAME), configure());
	}
}
