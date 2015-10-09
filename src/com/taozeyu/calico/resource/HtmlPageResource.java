package com.taozeyu.calico.resource;

import java.io.File;
import java.io.IOException;
import java.io.Reader;

class HtmlPageResource extends AbstractPageResource {

	HtmlPageResource(File resourceFile, String resourcePath) {
		super(resourceFile, resourcePath);
	}

	@Override
	public String createContent() throws IOException {
		try (Reader reader = createResourceFileReaderByJumpOverHead()) {
			return getStringFromReader(reader);
		}
	}

}
