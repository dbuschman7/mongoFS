package me.lightspeed7.mongofs;

import java.util.ArrayList;
import java.util.List;

public class MongoManifest {

    private MongoFile zip;

    private List<MongoFile> files = new ArrayList<MongoFile>();

    /**
     * Manifest file to hold all files for an expanded zip file
     * 
     * @param zip
     */
    public MongoManifest(final MongoFile zip) {
        this.zip = zip;
    }

    /**
     * Add a file to the manifest
     * 
     * @param file
     */
    public void addMongoFile(final MongoFile file) {
        this.files.add(file);
    }

    /**
     * Get all the files defined on this manifest
     * 
     * @return List<MongoFile>
     */
    public List<MongoFile> getFiles() {
        return files;
    }

    /**
     * Add a list of file to the Manifest
     * 
     * @param files
     */
    public void setFiles(final List<MongoFile> files) {
        this.files = files;
    }

    /**
     * Return the original file that the manifest represents the contents of
     * 
     * @return MongoFile
     */
    public MongoFile getZip() {
        return zip;
    }
}
