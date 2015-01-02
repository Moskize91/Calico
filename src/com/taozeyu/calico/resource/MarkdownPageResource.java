package com.taozeyu.calico.resource;

import java.io.File;
import java.io.IOException;
import java.io.Reader;

import com.petebevin.markdown.MarkdownProcessor;

public class MarkdownPageResource extends AbstractPageResource {

	private static final MarkdownProcessor MarkdwonProcessor = new MarkdownProcessor();
	
	MarkdownPageResource(File resourceFile) {
		super(resourceFile);
	}

	@Override
	public String getPageContent() throws IOException {
		Reader reader = createResourceFileReaderBuyJumpOverHead();
		try {
			String content = getStringFromReader(reader);
			return MarkdwonProcessor.markdown(content);
			
		} finally {
			reader.close();
		}
	}

}
