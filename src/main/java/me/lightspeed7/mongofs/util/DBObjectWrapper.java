package me.lightspeed7.mongofs.util;

import java.util.Date;

import org.bson.BSON;
import org.bson.types.ObjectId;

import com.mongodb.DBObject;

/**
 * Helper class to do the unwrapping, most of this methods were taken from the BasicBSONObject class in the MongoDB Java Driver
 * 
 * @author David Buschman
 * 
 *         No author info on BasicJSONObject
 * 
 */
public class DBObjectWrapper {

    private DBObject surrogate;

    public DBObjectWrapper(DBObject surrogate) {

        this.surrogate = surrogate;
    }

    /**
     * Returns the value of a field as an <code>int</code>.
     * 
     * @param key
     *            the field to look for
     * @return the field value (or default)
     */
    public int getInt(String key) {

        Object o = surrogate.get(key);
        if (o == null)
            throw new NullPointerException("no value for: " + key);

        return BSON.toInt(o);
    }

    /**
     * Returns the value of a field as an <code>int</code>.
     * 
     * @param key
     *            the field to look for
     * @param def
     *            the default to return
     * @return the field value (or default)
     */
    public int getInt(String key, int def) {

        Object foo = surrogate.get(key);
        if (foo == null)
            return def;

        return BSON.toInt(foo);
    }

    /**
     * Returns the value of a field as a <code>long</code>.
     * 
     * @param key
     *            the field to return
     * @return the field value
     */
    public long getLong(String key) {

        Object foo = surrogate.get(key);
        return ((Number) foo).longValue();
    }

    /**
     * Returns the value of a field as an <code>long</code>.
     * 
     * @param key
     *            the field to look for
     * @param def
     *            the default to return
     * @return the field value (or default)
     */
    public long getLong(String key, long def) {

        Object foo = surrogate.get(key);
        if (foo == null)
            return def;

        return ((Number) foo).longValue();
    }

    /**
     * Returns the value of a field as a <code>double</code>.
     * 
     * @param key
     *            the field to return
     * @return the field value
     */
    public double getDouble(String key) {

        Object foo = surrogate.get(key);
        return ((Number) foo).doubleValue();
    }

    /**
     * Returns the value of a field as an <code>double</code>.
     * 
     * @param key
     *            the field to look for
     * @param def
     *            the default to return
     * @return the field value (or default)
     */
    public double getDouble(String key, double def) {

        Object foo = surrogate.get(key);
        if (foo == null)
            return def;

        return ((Number) foo).doubleValue();
    }

    /**
     * Returns the value of a field as a string
     * 
     * @param key
     *            the field to look up
     * @return the value of the field, converted to a string
     */
    public String getString(String key) {

        Object foo = surrogate.get(key);
        if (foo == null)
            return null;
        return foo.toString();
    }

    /**
     * Returns the value of a field as a string
     * 
     * @param key
     *            the field to look up
     * @param def
     *            the default to return
     * @return the value of the field, converted to a string
     */
    public String getString(String key, final String def) {

        Object foo = surrogate.get(key);
        if (foo == null)
            return def;

        return foo.toString();
    }

    /**
     * Returns the value of a field as a boolean.
     * 
     * @param key
     *            the field to look up
     * @return the value of the field, or false if field does not exist
     */
    public boolean getBoolean(String key) {

        return getBoolean(key, false);
    }

    /**
     * Returns the value of a field as a boolean
     * 
     * @param key
     *            the field to look up
     * @param def
     *            the default value in case the field is not found
     * @return the value of the field, converted to a string
     */
    public boolean getBoolean(String key, boolean def) {

        Object foo = surrogate.get(key);
        if (foo == null)
            return def;
        if (foo instanceof Number)
            return ((Number) foo).intValue() > 0;
        if (foo instanceof Boolean)
            return ((Boolean) foo).booleanValue();
        throw new IllegalArgumentException("can't coerce to bool:" + foo.getClass());
    }

    /**
     * Returns the object id or null if not set.
     * 
     * @param field
     *            The field to return
     * @return The field object value or null if not found (or if null :-^).
     */
    public ObjectId getObjectId(final String field) {

        return (ObjectId) surrogate.get(field);
    }

    /**
     * Returns the object id or def if not set.
     * 
     * @param field
     *            The field to return
     * @param def
     *            the default value in case the field is not found
     * @return The field object value or def if not set.
     */
    public ObjectId getObjectId(final String field, final ObjectId def) {

        final Object foo = surrogate.get(field);
        return (foo != null) ? (ObjectId) foo : def;
    }

    /**
     * Returns the date or null if not set.
     * 
     * @param field
     *            The field to return
     * @return The field object value or null if not found.
     */
    public Date getDate(final String field) {

        return (Date) surrogate.get(field);
    }

    /**
     * Returns the date or def if not set.
     * 
     * @param field
     *            The field to return
     * @param def
     *            the default value in case the field is not found
     * @return The field object value or def if not set.
     */
    public Date getDate(final String field, final Date def) {

        final Object foo = surrogate.get(field);
        return (foo != null) ? (Date) foo : def;
    }

}
