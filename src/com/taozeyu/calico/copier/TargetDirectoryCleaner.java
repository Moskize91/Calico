package com.taozeyu.calico.copier;

import com.taozeyu.calico.GlobalConfig;

import java.io.File;
import java.util.regex.Pattern;

/**
 * Created by taozeyu on 15/10/6.
 */
public class TargetDirectoryCleaner {

    private static String[] ArtResourceExtendionNames = new String[] {
            "jpge", "jpg", "png", "gif"
    };
    private final Pattern ignoreCopyPattern = GlobalConfig.instance().getPattern("ignore-copy");
    private final Pattern ignoreCleanPattern = GlobalConfig.instance().getPattern("ignore-clean", ".*\\.git.*");
    private File targetPath;

    public TargetDirectoryCleaner(File targetPath) {
        this.targetPath = targetPath;
    }

    public void clean() {
        System.out.println("");
        System.out.println("Clean target directory: " + targetPath);
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
        return !isReservedFile(file) &&
               !isArtResourceFile(file) &&
               !ignoreCopyPattern.matcher(file.getPath()).find() &&
               !ignoreCleanPattern.matcher(file.getPath()).find();
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
