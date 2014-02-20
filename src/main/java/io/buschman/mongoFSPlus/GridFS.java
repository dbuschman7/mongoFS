package io.buschman.mongoFSPlus;

import io.buschman.mongoFSPlus.gridFS.GridFSDBFile;
import io.buschman.mongoFSPlus.gridFS.GridFSInputFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.bson.types.ObjectId;

import com.mongodb.DB;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public interface GridFS {

    /**
     * File's chunk size
     */
    public static final int DEFAULT_CHUNKSIZE = 256 * 1024;
    /**
     * Bucket to use for the collection namespaces
     */
    public static final String DEFAULT_BUCKET = "fs";

    /**
     * Gets the list of files stored in this gridfs, sorted by filename.
     * 
     * @return cursor of file objects
     */
    public abstract DBCursor getFileList();

    /**
     * Gets a filtered list of files stored in this gridfs, sorted by filename.
     * 
     * @param query
     *            filter to apply
     * @return cursor of file objects
     */
    public abstract DBCursor getFileList(DBObject query);

    /**
     * Gets a sorted, filtered list of files stored in this gridfs.
     * 
     * @param query
     *            filter to apply
     * @param sort
     *            sorting to apply
     * @return cursor of file objects
     */
    public abstract DBCursor getFileList(DBObject query, DBObject sort);

    /**
     * Finds one file matching the given objectId. Equivalent to findOne(objectId).
     * 
     * @param objectId
     *            the objectId of the file stored on a server
     * @return a gridfs file
     * @throws com.mongodb.MongoException
     */
    public abstract GridFSDBFile find(ObjectId objectId);

    /**
     * Finds one file matching the given objectId.
     * 
     * @param objectId
     *            the objectId of the file stored on a server
     * @return a gridfs file
     * @throws com.mongodb.MongoException
     */
    public abstract GridFSDBFile findOne(ObjectId objectId);

    /**
     * Finds one file matching the given filename.
     * 
     * @param filename
     *            the name of the file stored on a server
     * @return the gridfs db file
     * @throws com.mongodb.MongoException
     */
    public abstract GridFSDBFile findOne(String filename);

    /**
     * Finds one file matching the given query.
     * 
     * @param query
     *            filter to apply
     * @return a gridfs file
     * @throws com.mongodb.MongoException
     */
    public abstract GridFSDBFile findOne(DBObject query);

    /**
     * Finds a list of files matching the given filename.
     * 
     * @param filename
     *            the filename to look for
     * @return list of gridfs files
     * @throws com.mongodb.MongoException
     */
    public abstract List<GridFSDBFile> find(String filename);

    /**
     * Finds a list of files matching the given filename.
     * 
     * @param filename
     *            the filename to look for
     * @param sort
     *            the fields to sort with
     * @return list of gridfs files
     * @throws com.mongodb.MongoException
     */
    public abstract List<GridFSDBFile> find(String filename, DBObject sort);

    /**
     * Finds a list of files matching the given query.
     * 
     * @param query
     *            the filter to apply
     * @return list of gridfs files
     * @throws com.mongodb.MongoException
     */
    public abstract List<GridFSDBFile> find(DBObject query);

    /**
     * Finds a list of files matching the given query.
     * 
     * @param query
     *            the filter to apply
     * @param sort
     *            the fields to sort with
     * @return list of gridfs files
     * @throws com.mongodb.MongoException
     */
    public abstract List<GridFSDBFile> find(DBObject query, DBObject sort);

    /**
     * Removes the file matching the given id.
     * 
     * @param id
     *            the id of the file to be removed
     * @throws com.mongodb.MongoException
     */
    public abstract void remove(ObjectId id);

    /**
     * Removes all files matching the given filename.
     * 
     * @param filename
     *            the name of the file to be removed
     * @throws com.mongodb.MongoException
     */
    public abstract void remove(String filename);

    /**
     * Removes all files matching the given query.
     * 
     * @param query
     *            filter to apply
     * @throws com.mongodb.MongoException
     */
    public abstract void remove(DBObject query);

    /**
     * Creates a file entry. After calling this method, you have to call {@link com.mongodb.gridfs.GridFSInputFile#save()}.
     * 
     * @param data
     *            the file's data
     * @return a gridfs input file
     */
    public abstract GridFSInputFile createFile(byte[] data);

    /**
     * Creates a file entry. After calling this method, you have to call {@link com.mongodb.gridfs.GridFSInputFile#save()}.
     * 
     * @param f
     *            the file object
     * @return a gridfs input file
     * @throws IOException
     */
    public abstract GridFSInputFile createFile(File f)
            throws IOException;

    /**
     * Creates a file entry. After calling this method, you have to call {@link com.mongodb.gridfs.GridFSInputFile#save()}.
     * 
     * @param in
     *            an inputstream containing the file's data
     * @return a gridfs input file
     */
    public abstract GridFSInputFile createFile(InputStream in);

    /**
     * Creates a file entry. After calling this method, you have to call {@link com.mongodb.gridfs.GridFSInputFile#save()}.
     * 
     * @param in
     *            an inputstream containing the file's data
     * @param closeStreamOnPersist
     *            indicate the passed in input stream should be closed once the data chunk persisted
     * @return a gridfs input file
     */
    public abstract GridFSInputFile createFile(InputStream in, boolean closeStreamOnPersist);

    /**
     * Creates a file entry. After calling this method, you have to call {@link com.mongodb.gridfs.GridFSInputFile#save()}.
     * 
     * @param in
     *            an inputstream containing the file's data
     * @param filename
     *            the file name as stored in the db
     * @return a gridfs input file
     */
    public abstract GridFSInputFile createFile(InputStream in, String filename);

    /**
     * Creates a file entry. After calling this method, you have to call {@link com.mongodb.gridfs.GridFSInputFile#save()}.
     * 
     * @param in
     *            an inputstream containing the file's data
     * @param filename
     *            the file name as stored in the db
     * @param closeStreamOnPersist
     *            indicate the passed in input stream should be closed once the data chunk persisted
     * @return a gridfs input file
     */
    public abstract GridFSInputFile createFile(InputStream in, String filename, boolean closeStreamOnPersist);

    /**
     * Creates a file entry.
     * 
     * @param filename
     *            the file name as stored in the db
     * @return a gridfs input file
     * @see GridFS#createFile()
     */
    public abstract GridFSInputFile createFile(String filename);

    /**
     * This method creates an empty {@link GridFSInputFile} instance. On this instance an {@link java.io.OutputStream} can be
     * obtained using the {@link GridFSInputFile#getOutputStream()} method. You can still call
     * {@link GridFSInputFile#setContentType(String)} and {@link GridFSInputFile#setFilename(String)}. The file will be completely
     * written and closed after calling the {@link java.io.OutputStream#close()} method on the output stream.
     * 
     * @return GridFS file handle instance.
     */
    public abstract GridFSInputFile createFile();

    /**
     * Gets the bucket name used in the collection's namespace. Default value is 'fs'.
     * 
     * @return the name of the file bucket
     */
    public abstract String getBucketName();

    /**
     * Gets the database used.
     * 
     * @return the database
     */
    public abstract DB getDB();

}