/*
 * Copyright (c) 2008-2014 MongoDB, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package io.buschman.mongoFSPlus.legacy;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;

/**
 * Implementation of GridFS - a specification for storing and retrieving files that exceed the BSON-document size limit of 16MB.
 * <p/>
 * Instead of storing a file in a single document, GridFS divides a file into parts, or chunks, and stores each of those chunks as
 * a separate document. By default GridFS limits chunk size to 256k. GridFS uses two collections to store files. One collection
 * stores the file chunks, and the other stores file metadata.
 * <p/>
 * When you query a GridFS store for a file, the driver or client will reassemble the chunks as needed. You can perform range
 * queries on files stored through GridFS. You also can access information from arbitrary sections of files, which allows you to
 * ���skip��� into the middle of a video or audio file.
 * <p/>
 * GridFS is useful not only for storing files that exceed 16MB but also for storing any files for which you want access without
 * having to load the entire file into memory. For more information on the indications of GridFS, see MongoDB official
 * documentation.
 * 
 * @mongodb.driver.manual core/gridfs/ GridFS
 */
@SuppressWarnings( "rawtypes" )
public class GridFS implements io.buschman.mongoFSPlus.GridFS {

    /**
     * File's max chunk size
     * 
     * @deprecated You can calculate max chunkSize with a similar formula {@link com.mongodb.MongoClient#getMaxBsonObjectSize()} -
     *             500*1000. Please ensure that you left enough space for metadata (500kb is enough).
     */
    @Deprecated
    public static final long MAX_CHUNKSIZE = (long) (3.5 * 1000 * 1000);
    private final DB database;
    private final String bucketName;

    private final DBCollection filesCollection;
    private final DBCollection chunksCollection;

    /**
     * Creates a GridFS instance for the default bucket "fs" in the given database. Set the preferred WriteConcern on the give DB
     * with DB.setWriteConcern
     * 
     * @param db
     *            database to work with
     * @throws com.mongodb.MongoException
     * @see com.mongodb.WriteConcern
     */
    public GridFS(final DB db) {

        this(db, DEFAULT_BUCKET);
    }

    /**
     * Creates a GridFS instance for the specified bucket in the given database. Set the preferred WriteConcern on the give DB
     * with DB.setWriteConcern
     * 
     * @param db
     *            database to work with
     * @param bucket
     *            bucket to use in the given database
     * @throws com.mongodb.MongoException
     * @see com.mongodb.WriteConcern
     */
    public GridFS(final DB db, final String bucket) {

        this.database = db;
        this.bucketName = bucket;

        this.filesCollection = database.getCollection(bucketName + ".files");
        this.chunksCollection = database.getCollection(bucketName + ".chunks");

        // ensure standard indexes as long as collections are small
        try {
            if (filesCollection.count() < 1000) {
                filesCollection.createIndex(new BasicDBObject("filename", 1).append("uploadDate", 1));
            }
            if (chunksCollection.count() < 1000) {
                chunksCollection.createIndex(new BasicDBObject("files_id", 1).append("n", 1), new BasicDBObject(
                        "unique", true));
            }
        } catch (MongoException e) {
            // TODO: Logging
        }

        filesCollection.setObjectClass(GridFSDBFile.class);
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.buschman.mongoFSPlus.legacy.IGridFS#getFileList()
     */
    @Override
    public DBCursor getFileList() {

        return filesCollection.find().sort(new BasicDBObject("filename", 1));
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.buschman.mongoFSPlus.legacy.IGridFS#getFileList(com.mongodb.DBObject)
     */
    @Override
    public DBCursor getFileList(final DBObject query) {

        return filesCollection.find(query).sort(new BasicDBObject("filename", 1));
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.buschman.mongoFSPlus.legacy.IGridFS#getFileList(com.mongodb.DBObject, com.mongodb.DBObject)
     */
    @Override
    public DBCursor getFileList(final DBObject query, final DBObject sort) {

        return filesCollection.find(query).sort(sort);
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.buschman.mongoFSPlus.legacy.IGridFS#find(org.bson.types.ObjectId)
     */
    @Override
    public GridFSDBFile find(final ObjectId objectId) {

        return findOne(objectId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.buschman.mongoFSPlus.legacy.IGridFS#findOne(org.bson.types.ObjectId)
     */
    @Override
    public GridFSDBFile findOne(final ObjectId objectId) {

        return findOne(new BasicDBObject("objectId", objectId));
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.buschman.mongoFSPlus.legacy.IGridFS#findOne(java.lang.String)
     */
    @Override
    public GridFSDBFile findOne(final String filename) {

        return findOne(new BasicDBObject("filename", filename));
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.buschman.mongoFSPlus.legacy.IGridFS#findOne(com.mongodb.DBObject)
     */
    @Override
    public GridFSDBFile findOne(final DBObject query) {

        return injectGridFSInstance(filesCollection.findOne(query));
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.buschman.mongoFSPlus.legacy.IGridFS#find(java.lang.String)
     */
    @Override
    public List<GridFSDBFile> find(final String filename) {

        return find(new BasicDBObject("filename", filename));
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.buschman.mongoFSPlus.legacy.IGridFS#find(java.lang.String, com.mongodb.DBObject)
     */
    @Override
    public List<GridFSDBFile> find(final String filename, final DBObject sort) {

        return find(new BasicDBObject("filename", filename), sort);
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.buschman.mongoFSPlus.legacy.IGridFS#find(com.mongodb.DBObject)
     */
    @Override
    public List<GridFSDBFile> find(final DBObject query) {

        return find(query, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.buschman.mongoFSPlus.legacy.IGridFS#find(com.mongodb.DBObject, com.mongodb.DBObject)
     */
    @Override
    public List<GridFSDBFile> find(final DBObject query, final DBObject sort) {

        List<GridFSDBFile> files = new ArrayList<GridFSDBFile>();

        DBCursor cursor = filesCollection.find(query);
        if (sort != null) {
            cursor.sort(sort);
        }

        try {
            while (cursor.hasNext()) {
                files.add(injectGridFSInstance(cursor.next()));
            }
        } finally {
            cursor.close();
        }
        return Collections.unmodifiableList(files);
    }

    private GridFSDBFile injectGridFSInstance(final Object o) {

        if (o == null) {
            return null;
        }

        if (!(o instanceof GridFSDBFile)) {
            throw new IllegalArgumentException("somehow didn't get a GridFSDBFile");
        }

        GridFSDBFile f = (GridFSDBFile) o;
        f.fs = this;
        return f;
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.buschman.mongoFSPlus.legacy.IGridFS#remove(org.bson.types.ObjectId)
     */
    @Override
    public void remove(final ObjectId id) {

        if (id == null) {
            throw new IllegalArgumentException("file id can not be null");
        }

        filesCollection.remove(new BasicDBObject("_id", id));
        chunksCollection.remove(new BasicDBObject("files_id", id));
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.buschman.mongoFSPlus.legacy.IGridFS#remove(java.lang.String)
     */
    @Override
    public void remove(final String filename) {

        if (filename == null) {
            throw new IllegalArgumentException("filename can not be null");
        }

        remove(new BasicDBObject("filename", filename));
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.buschman.mongoFSPlus.legacy.IGridFS#remove(com.mongodb.DBObject)
     */
    @Override
    public void remove(final DBObject query) {

        if (query == null) {
            throw new IllegalArgumentException("query can not be null");
        }

        for (final GridFSDBFile f : find(query)) {
            f.remove();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.buschman.mongoFSPlus.legacy.IGridFS#createFile(byte[])
     */
    @Override
    public GridFSInputFile createFile(final byte[] data) {

        return createFile(new ByteArrayInputStream(data), true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.buschman.mongoFSPlus.legacy.IGridFS#createFile(java.io.File)
     */
    @Override
    public GridFSInputFile createFile(final File f)
            throws IOException {

        return createFile(new FileInputStream(f), f.getName(), true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.buschman.mongoFSPlus.legacy.IGridFS#createFile(java.io.InputStream)
     */
    @Override
    public GridFSInputFile createFile(final InputStream in) {

        return createFile(in, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.buschman.mongoFSPlus.legacy.IGridFS#createFile(java.io.InputStream, boolean)
     */
    @Override
    public GridFSInputFile createFile(final InputStream in, final boolean closeStreamOnPersist) {

        return createFile(in, null, closeStreamOnPersist);
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.buschman.mongoFSPlus.legacy.IGridFS#createFile(java.io.InputStream, java.lang.String)
     */
    @Override
    public GridFSInputFile createFile(final InputStream in, final String filename) {

        return new GridFSInputFile(this, in, filename);
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.buschman.mongoFSPlus.legacy.IGridFS#createFile(java.io.InputStream, java.lang.String, boolean)
     */
    @Override
    public GridFSInputFile createFile(final InputStream in, final String filename, final boolean closeStreamOnPersist) {

        return new GridFSInputFile(this, in, filename, closeStreamOnPersist);
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.buschman.mongoFSPlus.legacy.IGridFS#createFile(java.lang.String)
     */
    @Override
    public GridFSInputFile createFile(final String filename) {

        return new GridFSInputFile(this, filename);
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.buschman.mongoFSPlus.legacy.IGridFS#createFile()
     */
    @Override
    public GridFSInputFile createFile() {

        return new GridFSInputFile(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.buschman.mongoFSPlus.legacy.IGridFS#getBucketName()
     */
    @Override
    public String getBucketName() {

        return bucketName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.buschman.mongoFSPlus.legacy.IGridFS#getDB()
     */
    @Override
    public DB getDB() {

        return database;
    }

    /**
     * Gets the {@link DBCollection} in which the file's metadata is stored.
     * 
     * @return the collection
     */
    DBCollection getFilesCollection() {

        return filesCollection;
    }

    /**
     * Gets the {@link DBCollection} in which the binary chunks are stored.
     * 
     * @return the collection
     */
    DBCollection getChunksCollection() {

        return chunksCollection;
    }

}
