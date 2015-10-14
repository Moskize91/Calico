package com.taozeyu.calico.resource;

import com.taozeyu.calico.util.PathUtil;

import java.io.File;
import java.io.IOException;
import java.io.Reader;

/**
 * Created by taozeyu on 15/10/9.
 */
abstract class AbstractResource<CONTENT> extends ResourceFileWithHead {

    private final String resourcePath;
    private CONTENT contentCache;

    AbstractResource(File resourceFile, String resourcePath) {
        super(resourceFile);
        this.resourcePath = resourcePath;
    }

    protected abstract CONTENT createContent() throws IOException;

    protected final CONTENT getContent() throws IOException {
        if (contentCache == null) {
            contentCache = createContent();
        }
        return contentCache;
    }

    protected final String getStringFromReader(Reader reader) throws IOException {
        StringBuilder sb = new StringBuilder();
        int ch;
        while((ch = reader.read()) >= 0) {
            sb.append((char) ch);
        }
        return sb.toString();
    }

    public String getPath() {
        return PathUtil.clearExtensionName(resourcePath);
    }

    public String getName() {
        return PathUtil.clearExtensionName(getFullName());
    }

    public String getFullName() {
        return resourceFile.getName();
    }
}
