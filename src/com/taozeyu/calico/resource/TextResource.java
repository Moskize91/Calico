package com.taozeyu.calico.resource;

import java.io.File;
import java.io.IOException;
import java.io.Reader;

/**
 * Created by taozeyu on 15/10/9.
 */
public class TextResource extends AbstractResource<String> {

    TextResource(File resourceFile, String resourcePath) {
        super(resourceFile, resourcePath);
    }

    @Override
    protected String createContent() throws IOException {
        try (Reader reader = createResourceFileReaderByJumpOverHead()) {
            return getStringFromReader(reader);
        }
    }

    public String getTextContent() throws IOException {
        return getContent();
    }

    public String getTextContentAsOneLine() throws IOException {
        return getContent().replaceAll("\\s+", " ");
    }
}
