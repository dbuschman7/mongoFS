package me.lightspeed7.mongofs.util;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class JSONHelper {

    /**
     * Method to pretty print a JSON string got easier readability
     * 
     * @param marshal
     * @return the pretty printed version
     */
    public static final String prettyPrint(String marshal) {

        JsonParser jp = new JsonParser();
        JsonElement je = jp.parse(marshal);
        return new GsonBuilder().setPrettyPrinting().create().toJson(je);

    }
}
