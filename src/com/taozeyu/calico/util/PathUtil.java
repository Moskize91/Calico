package com.taozeyu.calico.util;

import java.io.File;

/**
 * Created by taozeyu on 15/10/9.
 */
public class PathUtil {

    public static boolean isAbsolutePath(String path) {
        return path.matches("([a-zA-Z]+:)?(\\\\|/).*");
    }

    public static  String normalizePath(String absolutePath) {
        return absolutePath.replaceAll("\\\\", "/").replaceAll("(\\.(\\w|\\-)+/?)?$", "");
    }

    public static File getFile(String path, String currentDirectoryPath) {
        if (isAbsolutePath(path)) {
            return new File(path);
        }
        path = path.replaceFirst("^\\.(/|\\\\)", "");
        return new File(currentDirectoryPath, path);
    }

}
