package me.lightspeed7.mongofs.spring;

import java.net.UnknownHostException;

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
@ComponentScan( "me.lightspeed7.mongofs.spring" )
public class JavaConfig {

    static final String COLLECTION_NAME = "MongoFS-JavaConfig";

    @Bean( name = "mongoClient" )
    public MongoClient client() {

        try {
            return new MongoClient(new MongoClientURI("mongodb://cayman-vm:27017")); // my vm server
        } catch (UnknownHostException e) {
            System.out.println("Cayman-vm unavailable, trying localhost");
            try {
                return new MongoClient(new MongoClientURI("mongodb://localhost:27017")); // most others
            } catch (UnknownHostException ex) {
                throw new IllegalArgumentException("Unable to connect a mongoDB instance", ex);
            }
        }
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

    @Bean( name = "mongoFileStore" )
    public MongoFileStore store() {

        return new MongoFileStore(client().getDB(COLLECTION_NAME), configure());
    }
}
