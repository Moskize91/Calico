package com.taozeyu.calico.resource;

import java.io.File;
import java.io.IOException;
import java.io.Reader;

public abstract class AbstractPageResource extends ResourceFileWithHead {

	private String contentCache = null;

	AbstractPageResource(File resourceFile) {
		super(resourceFile);
	}

	public String getContent() throws IOException {
		if (contentCache == null) {
			contentCache = createContent();
		}
		return contentCache;
	}

	protected abstract String createContent() throws IOException;

	public String getName() {
		return clearExtensionName(getFullName());
	}

	public String getFullName() {
		return resourceFile.getName();
	}

	protected String getStringFromReader(Reader reader) throws IOException {
		StringBuilder sb = new StringBuilder();
		int ch;
		while((ch = reader.read()) >= 0) {
			sb.append((char) ch);
		}
		return sb.toString();
	}

	private String clearExtensionName(String path) {
		return path.replaceAll("\\.(\\w|\\-)+$", "");
	}
}
