package me.lightspeed7.mongofs.common;

public interface InputFile {

    /**
     * Associates a key with a value in the current map object.
     * 
     * @param key
     * @param value
     * 
     * @return the previous value for the key if any
     */
    public Object put(String key, Object value);

    /**
     * Returns the value of a field on the object
     * 
     * @param string
     * @return
     */
    public Object get(String string);

}