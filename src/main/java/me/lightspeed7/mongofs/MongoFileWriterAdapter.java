package me.lightspeed7.mongofs;

import me.lightspeed7.mongofs.writing.ChunksStatisticsAdapter;

/**
 * Adapter to handle the custom pars of data collection from each chunk
 * 
 * @author David Buschman
 * 
 */
public class MongoFileWriterAdapter extends ChunksStatisticsAdapter {

    private MongoFile file;

    public MongoFileWriterAdapter(MongoFile file) {

        super(file);
        this.file = file;
    }

    @Override
    public void close() {

        super.close();
        file.save();

        file.validate();
    }
}
