package org.mongodb.file;

import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;
import me.lightspeed7.mongofs.util.ChunkSize;
import me.lightspeed7.mongofs.crypto.BasicCrypto;

import spock.lang.Specification;

class MongoFileStoreConfigSpecification extends Specification {

    def "should create config with defaults"() {
        expect:
        def config = MongoFileStoreConfig.builder().build();

        'fileStore' == config.getBucket();
        MongoFileStoreConfig.DEFAULT_CHUNKSIZE.getChunkSize() == config.getChunkSize();
        ReadPreference.primary() == config.getReadPreference();
        WriteConcern.JOURNALED == config.getWriteConcern();
        true == config.isAsyncDeletes();
        true == config.isCompressionEnabled();
        false == config.isEncryptionEnabled();
    }

    def "should create config with non-defaults, encryption, no compression"() {
        expect:
        def config = MongoFileStoreConfig.builder().asyncDeletes(false).bucket('foo')//
                .chunkSize(ChunkSize.huge_4M).enableCompression(false)//
                .readPreference(ReadPreference.SECONDARY_PREFERRED).writeConcern(WriteConcern.FSYNCED)//
                .build();

        'foo' == config.getBucket();
        ChunkSize.huge_4M.getChunkSize() == config.getChunkSize();
        ReadPreference.secondaryPreferred() == config.getReadPreference();
        WriteConcern.FSYNCED == config.getWriteConcern();
        false == config.isAsyncDeletes();
        false == config.isCompressionEnabled();
        true == config.isEncryptionEnabled();

    }

    def "should create config with encryption if compression is disabled"() {
        expect:
        def config = MongoFileStoreConfig.builder().asyncDeletes(false).bucket('foo')//
                .chunkSize(ChunkSize.huge_4M).enableCompression(false).enableEncryption(new BasicCrypto())//
                .readPreference(ReadPreference.SECONDARY_PREFERRED).writeConcern(WriteConcern.FSYNCED)//
                .build();

        'foo' == config.getBucket();
        ChunkSize.huge_4M.getChunkSize() == config.getChunkSize();
        ReadPreference.secondaryPreferred() == config.getReadPreference();
        WriteConcern.FSYNCED == config.getWriteConcern();
        false == config.isAsyncDeletes();
        false == config.isCompressionEnabled();
        true == config.isEncryptionEnabled();
    }

     def "should throw when compression and encryption are both enabled - encryption first"() {
        when:
        MongoFileStoreConfig.builder().encryption(new BasicCrypto()).enableCompression(tree)//
                .build();

        then:
        thrown(IllegalStateException)
    }

    def "should throw when compression and encryption are both enabled - compression first"() {
        when:
        MongoFileStoreConfig.builder().enableCompression(tree).encryption(new BasicCrypto())//
                .build();

        then:
        thrown(IllegalStateException)
    }

    def "should be compatible with existing GridFS filestores"()  {
        when:
        def config = MongoFileStoreConfig.builder().gridFSCompatible('test');

        then:
        'test' == config.getBucket()
        ChunkSize.medium_256K == config.getChunkSize();
        null == config.getWriteConcern();
        null == config.getReadPreference();
        false == config.isCompressionEnabled();
        false == config.isAsyncDeletes();
        false == config.isEncryptionEnabled();
    }

}
