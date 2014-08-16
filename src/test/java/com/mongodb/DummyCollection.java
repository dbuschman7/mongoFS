package com.mongodb;

import java.util.Iterator;
import java.util.List;

public class DummyCollection extends DBCollection {

    public DummyCollection(DB base, String name) {

        super(base, name);

    }

    public List<DBObject> list;
    public DBObject q;
    public DBObject o;

    @Override
    public WriteResult insert(List<DBObject> list, WriteConcern concern, DBEncoder encoder) {

        this.list = list;
        return null;
    }

    @Override
    public WriteResult update(DBObject q, DBObject o, boolean upsert, boolean multi, WriteConcern concern, DBEncoder encoder) {

        this.q = q;
        this.o = o;
        return null;
    }

    @Override
    protected void doapply(DBObject o) {

        this.o = o;

    }

    @Override
    public WriteResult remove(DBObject o, WriteConcern concern, DBEncoder encoder) {

        this.o = o;
        return null;
    }

    // @Override
    Iterator<DBObject> __find(DBObject ref, DBObject fields, int numToSkip, int batchSize, int limit, int options, ReadPreference readPref,
            DBDecoder decoder) {

        // TODO Auto-generated method stub
        return null;
    }

    // @Override
    Iterator<DBObject> __find(DBObject ref, DBObject fields, int numToSkip, int batchSize, int limit, int options, ReadPreference readPref,
            DBDecoder decoder, DBEncoder encoder) {

        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void createIndex(DBObject keys, DBObject options, DBEncoder encoder) {

        // TODO Auto-generated method stub

    }

    @Override
    QueryResultIterator find(DBObject ref, DBObject fields, int numToSkip, int batchSize, int limit, int options, ReadPreference readPref,
            DBDecoder decoder) {

        // TODO Auto-generated method stub
        return null;
    }

    @Override
    QueryResultIterator find(DBObject ref, DBObject fields, int numToSkip, int batchSize, int limit, int options, ReadPreference readPref,
            DBDecoder decoder, DBEncoder encoder) {

        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Cursor aggregate(List<DBObject> pipeline, AggregationOptions options, ReadPreference readPreference) {

        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Cursor> parallelScan(ParallelScanOptions options) {

        // TODO Auto-generated method stub
        return null;
    }

    @Override
    BulkWriteResult executeBulkWriteOperation(boolean ordered, List<WriteRequest> requests, WriteConcern writeConcern, DBEncoder encoder) {

        // TODO Auto-generated method stub
        return null;
    }
}
