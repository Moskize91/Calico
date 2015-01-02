package com.taozeyu.calico.resource;

import java.io.File;
import java.io.IOException;
import java.io.Reader;

class HtmlPageResource extends AbstractPageResource {

	HtmlPageResource(File resourceFile) {
		super(resourceFile);
	}

	@Override
	public String getContent() throws IOException {
		Reader reader = createResourceFileReaderBuyJumpOverHead();
		try {
			return getStringFromReader(reader);
		} finally {
			reader.close();
		}
	}

}
