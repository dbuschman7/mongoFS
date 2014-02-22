package me.lightspeed7.mongofs.common;

import com.mongodb.BasicDBObject;

/**
 * 
 * @author David Buschman
 * 
 */
public interface ChunksStatisticsAdapter {

    // a new chunk is created and will be persisted
    public void collectFromChunk(BasicDBObject obj);

    // the stream on data has closed
    public void close();

    // the stream has been flushed
    public void flush();
}
