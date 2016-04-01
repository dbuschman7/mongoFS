package org.mongodb;

public class WriteResult {

    private com.mongodb.WriteResult surrogate;

    public WriteResult(final com.mongodb.WriteResult in) {
        this.surrogate = in;
    }

    public int getCount() {
        return surrogate.getN();
    }

}
