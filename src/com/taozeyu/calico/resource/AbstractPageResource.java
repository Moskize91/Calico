package com.taozeyu.calico.resource;

import java.io.File;
import java.io.IOException;
import java.io.Reader;

public abstract class AbstractPageResource extends ResourceFileWithHead {

	AbstractPageResource(File resourceFile) {
		super(resourceFile);
	}

	public abstract String getPageContent() throws IOException;
	
	protected String getStringFromReader(Reader reader) throws IOException {
		StringBuilder sb = new StringBuilder();
		int ch;
		while((ch = reader.read()) >= 0) {
			sb.append((char) ch);
		}
		return sb.toString();
	}
}
