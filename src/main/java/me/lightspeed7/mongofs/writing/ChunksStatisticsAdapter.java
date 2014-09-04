package me.lightspeed7.mongofs.writing;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import me.lightspeed7.mongofs.MongoFileConstants;
import me.lightspeed7.mongofs.util.FileUtil;

import org.mongodb.Document;

/**
 * 
 * @author David Buschman
 * 
 */
public abstract class ChunksStatisticsAdapter {

    private InputFile file;
    private MessageDigest messageDigest;
    private int chunkCount;
    private long totalSize;

    public ChunksStatisticsAdapter(final InputFile file) {

        this.file = file;
        try {
            this.messageDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("No MD5!");
        }
    }

    public void collectFromChunk(final Document obj) {

        byte[] data = (byte[]) obj.get("data");

        // accumulate
        ++chunkCount;
        this.messageDigest.update(data);
        this.totalSize += data.length;
    }

    public void close() {

        file.put(MongoFileConstants.chunkCount.name(), chunkCount);
        file.put(MongoFileConstants.length.name(), totalSize);
        file.put(MongoFileConstants.md5.name(), FileUtil.toHex(messageDigest.digest()));
    }

    public void flush() {

        // no-op

    }
}
