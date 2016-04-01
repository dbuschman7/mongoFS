package me.lightspeed7.mongofs.gridfs;

import me.lightspeed7.mongofs.ChunksStatisticsAdapter;

/**
 * Adapter to handle the custom pars of data collection from each chunk
 * 
 * @author David Buschman
 * 
 */
public class GridFSInputFileAdapter extends ChunksStatisticsAdapter {

    private GridFSInputFile file;

    public GridFSInputFileAdapter(GridFSInputFile file) {

        super(file);
        this.file = file;
    }

    @Override
    public void close() {

        super.close();
        file.superSave();
    }
}
