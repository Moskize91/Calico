package com.taozeyu.calico;

import java.io.File;
import java.util.Map;

/**
 * Created by taozeyu on 16/7/10.
 */
public class RuntimeContext {

    private File templateDirectory;
    private File systemEntityDirectory;
    private Map<String, File> librerayDirectoryMap;

    public File getTemplateDirectory() {
        return templateDirectory;
    }

    public void setTemplateDirectory(File templateDirectory) {
        this.templateDirectory = templateDirectory;
    }

    public File getSystemEntityDirectory() {
        return systemEntityDirectory;
    }

    public void setSystemEntityDirectory(File systemEntityDirectory) {
        this.systemEntityDirectory = systemEntityDirectory;
    }

    public Map<String, File> getLibrerayDirectoryMap() {
        return librerayDirectoryMap;
    }

    public void setLibrerayDirectoryMap(Map<String, File> librerayDirectoryMap) {
        this.librerayDirectoryMap = librerayDirectoryMap;
    }
}
