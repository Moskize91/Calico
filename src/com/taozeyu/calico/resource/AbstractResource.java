package com.taozeyu.calico.resource;

import com.taozeyu.calico.util.PathUtil;

import java.io.File;
import java.io.IOException;

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

    protected CONTENT getContent() throws IOException {
        if (contentCache == null) {
            contentCache = createContent();
        }
        return contentCache;
    }

    public String getPath() {
        return resourcePath;
    }

    public String getName() {
        return PathUtil.clearExtensionName(getFullName());
    }

    public String getFullName() {
        return resourceFile.getName();
    }
}
