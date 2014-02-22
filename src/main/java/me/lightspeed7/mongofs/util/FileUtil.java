package me.lightspeed7.mongofs.util;

import java.io.File;

public class FileUtil {

    public static String getExtension(File f) {

        return getExtension(f.getName());
    }

    public static String getExtension(String f) {

        return f.substring(f.lastIndexOf('.') + 1);
    }
}
