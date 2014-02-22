package me.lightspeed7.mongofs.gridfs;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import me.lightspeed7.mongofs.common.ChunksStatisticsAdapter;
import me.lightspeed7.mongofs.common.MongoFileConstants;

import com.mongodb.BasicDBObject;
import com.mongodb.util.Util;

/**
 * Adapter to handle the custom pars of data collection from each chunk
 * 
 * @author David Buschman
 * 
 */
public class GridFSInputFileAdapter implements ChunksStatisticsAdapter {

    private GridFSInputFile file;
    private MessageDigest messageDigest;
    private int chunkCount;
    private long totalSize;

    public GridFSInputFileAdapter(GridFSInputFile file) {

        this.file = file;
        try {
            this.messageDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("No MD5!");
        }
    }

    @Override
    public void collectFromChunk(BasicDBObject obj) {

        byte[] data = (byte[]) obj.get("data");

        // accumulate
        ++chunkCount;
        this.messageDigest.update(data);
        this.totalSize += data.length;
    }

    @Override
    public void close() {

        file.put(MongoFileConstants.chunkCount.name(), chunkCount);
        file.put(MongoFileConstants.length.name(), totalSize);
        file.put(MongoFileConstants.md5.name(), Util.toHex(messageDigest.digest()));
        file.superSave();
    }

    @Override
    public void flush() {

        // no-op

    }
}
