package me.lightspeed7.mongofs.util;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public final class JSONHelper {

    /**
     * Method to pretty print a JSON string got easier readability
     * 
     * @param marshal
     * @return the pretty printed version
     */
    public static String prettyPrint(final String marshal) {

        JsonParser jp = new JsonParser();
        JsonElement je = jp.parse(marshal);
        return new GsonBuilder().setPrettyPrinting().create().toJson(je);

    }

    private JSONHelper() {
        // empty
    }
}
