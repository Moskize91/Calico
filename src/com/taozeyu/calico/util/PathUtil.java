package com.taozeyu.calico.util;

import java.io.File;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by taozeyu on 15/10/9.
 */
public class PathUtil {

    public static boolean isAbsolutePath(String path) {
        return path.matches("([a-zA-Z]+:)?(\\\\|/).*");
    }

    public static String normalizePath(String absolutePath) {
        return absolutePath.replaceAll("\\\\", "/");
    }

    public static String toUnixLikeStylePath(String path) {
        if (isAbsolutePath(path)) {
            path = path.replaceAll("^[a-zA-Z]+:]", "");
        }
        return normalizePath(path);
    }

    public static String[] splitComponents(String path) {
        return path.replaceAll("(^(/|\\\\)|(/|\\\\)$)", "").split("(/|\\\\)");
    }

    public static String pathFromComponents(String[] components, boolean isRoot) {
        String path = isRoot? "/": "";
        for (int i = 0; i < components.length; i ++) {
            path += components[i];
            if (i < components.length - 1) {
                path += "/";
            }
        }
        return path;
    }

    public static  String normalizePathAndCleanExtensionName(String absolutePath) {
        return normalizePath(absolutePath).replaceAll("(\\.(\\w|_|\\-)+/?)?$", "");
    }

    public static File getFile(String path, String currentDirectoryPath) {
        if (isAbsolutePath(path)) {
            return new File(path);
        }
        path = path.replaceFirst("^\\.(/|\\\\)", "");
        return new File(currentDirectoryPath, path);
    }

    public static String pathMerge(String absolutionPath, String path) {
        if (!isAbsolutePath(absolutionPath)) {
            return null;
        }
        path = toUnixLikeStylePath(path);
        if (isAbsolutePath(path)) {
            return path;
        }
        String[] targetComponents = splitComponents(path);
        LinkedList<String> components = new LinkedList<>();
        for (String c : splitComponents(absolutionPath)) {
            components.add(c);
        }
        for (String tc : targetComponents) {
            if (tc.equals(".")) {

            } else if (tc.equals("..")) {
                if (components.isEmpty()) {
                    return null;
                }
                components.removeLast();
            } else {
                components.add(tc);
            }
        }
        boolean isRoot = true;
        path = pathFromComponents(
                components.toArray(new String[components.size()]), isRoot
        );
        return path;
    }

    public static String removeLastPathComponent(String path) {
        return path.replaceAll("/?(\\.|\\w|_|\\-)+/?$", "");
    }

    public static String getExtensionName(String path) {
        return getExtensionName(path, "html");
    }

    public static String getExtensionName(String path, String holderExtensionName) {
        String extensionName;
        Matcher matcher = Pattern.compile("\\.(\\w|\\-)+$").matcher(path);
        if(matcher.find()) {
            extensionName = matcher.group().replaceAll("^\\.", "");
        } else {
            extensionName = holderExtensionName;
        }
        return extensionName;
    }

    public static String clearExtensionName(String path) {
        String clearedPath = path;
        Matcher matcher = Pattern.compile("\\.(\\w|_|\\-)+$").matcher(path);
        if (matcher.find()) {
            clearedPath = matcher.replaceAll("");
        }
        return clearedPath;
    }
}
