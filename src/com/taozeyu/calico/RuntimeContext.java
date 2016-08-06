package com.taozeyu.calico;

import java.io.File;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by taozeyu on 16/7/10.
 */
public class RuntimeContext {

    private int port;
    private File templateDirectory;
    private File targetDirectory;
    private File resourceDirectory;
    private File systemEntityDirectory;
    private String rootPage;
    private String[] seeds;
    private String[] resourceAssetsPath;
    private Pattern ignoreCopy;
    private Pattern ignoreClean;
    private Map<String, String> redirectMap;
    private Map<String, File> libraryDirectoryMap;

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public File getTemplateDirectory() {
        return templateDirectory;
    }

    public void setTemplateDirectory(File templateDirectory) {
        this.templateDirectory = templateDirectory;
    }

    public File getTargetDirectory() {
        return targetDirectory;
    }

    public void setTargetDirectory(File targetDirectory) {
        this.targetDirectory = targetDirectory;
    }

    public File getResourceDirectory() {
        return resourceDirectory;
    }

    public void setResourceDirectory(File resourceDirectory) {
        this.resourceDirectory = resourceDirectory;
    }

    public File getSystemEntityDirectory() {
        return systemEntityDirectory;
    }

    public void setSystemEntityDirectory(File systemEntityDirectory) {
        this.systemEntityDirectory = systemEntityDirectory;
    }

    public String getRootPage() {
        return rootPage;
    }

    public void setRootPage(String rootPage) {
        this.rootPage = rootPage;
    }

    public String[] getSeeds() {
        return seeds;
    }

    public void setSeeds(String[] seeds) {
        this.seeds = seeds;
    }

    public String[] getResourceAssetsPath() {
        return resourceAssetsPath;
    }

    public void setResourceAssetsPath(String[] resourceAssetsPath) {
        this.resourceAssetsPath = resourceAssetsPath;
    }

    public Pattern getIgnoreCopy() {
        return ignoreCopy;
    }

    public void setIgnoreCopy(Pattern ignoreCopy) {
        this.ignoreCopy = ignoreCopy;
    }

    public Pattern getIgnoreClean() {
        return ignoreClean;
    }

    public void setIgnoreClean(Pattern ignoreClean) {
        this.ignoreClean = ignoreClean;
    }

    public Map<String, String> getRedirectMap() {
        return redirectMap;
    }

    public void setRedirectMap(Map<String, String> redirectMap) {
        this.redirectMap = redirectMap;
    }

    public Map<String, File> getLibraryDirectoryMap() {
        return libraryDirectoryMap;
    }

    public void setLibraryDirectoryMap(Map<String, File> libraryDirectoryMap) {
        this.libraryDirectoryMap = libraryDirectoryMap;
    }
}
