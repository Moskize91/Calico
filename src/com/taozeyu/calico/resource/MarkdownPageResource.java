package com.taozeyu.calico.resource;

import java.io.File;
import java.io.IOException;
import java.io.Reader;

import com.petebevin.markdown.MarkdownProcessor;

public class MarkdownPageResource extends AbstractPageResource {

	private static final MarkdownProcessor MarkdwonProcessor = new MarkdownProcessor();
	
	MarkdownPageResource(File resourceFile, String resourcePath) {
		super(resourceFile, resourcePath);
	}

	@Override
	public String createContent() throws IOException {
		try (Reader reader = createResourceFileReaderByJumpOverHead()) {
			String content = getStringFromReader(reader);
			return MarkdwonProcessor.markdown(content);
		}
	}

}
