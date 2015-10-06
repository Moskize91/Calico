package com.taozeyu.calico.cleaner;

import java.io.File;

/**
 * Created by taozeyu on 15/10/6.
 */
public class TargetDirectoryCleaner {

    private static String[] ArtResourceExtendionNames = new String[] {
            "jpge", "jpg", "png", "gif"
    };
    private File targetPath;

    public TargetDirectoryCleaner(File targetPath) {
        this.targetPath = targetPath;
    }

    public void clean() {
        System.out.println("");
        System.out.println("Clean target directory: "+ targetPath);
        cleanDirectory(targetPath);
    }

    private void cleanDirectory(File file) {
        if (file.isDirectory()) {
            for (File childFile : file.listFiles()) {
                cleanDirectory(childFile);
            }
        } else {
            if (fileShouldBeCleaned(file)) {
                System.out.println("\t delete file "+ file.getPath());
                file.delete();
            }
        }
    }

    private boolean fileShouldBeCleaned(File file) {
        return !isReservedFile(file) && !isArtResourceFile(file);
    }

    private boolean isReservedFile(File file) {
        return file.getName().startsWith(".");
    }

    private boolean isArtResourceFile(File file) {
        for (String ext : ArtResourceExtendionNames) {
            if (file.getName().matches(".*\\."+ ext +"$")) {
                return true;
            }
        }
        return false;
    }
}
