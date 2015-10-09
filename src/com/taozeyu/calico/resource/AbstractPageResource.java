package com.taozeyu.calico.resource;

import com.taozeyu.calico.util.PathUtil;
import org.jsoup.Jsoup;

import java.io.File;
import java.io.IOException;
import java.io.Reader;

public abstract class AbstractPageResource extends ResourceFileWithHead {

	private final String resourcePath;
	private String contentCache = null;

	AbstractPageResource(File resourceFile, String resourcePath) {
		super(resourceFile);
		this.resourcePath = resourcePath;
	}

	public String getHtmlContent() throws IOException {
		if (contentCache == null) {
			contentCache = createHtmlContent();
		}
		return contentCache;
	}

	protected abstract String createHtmlContent() throws IOException;

	public String getTextContent() throws IOException {
		return Jsoup.parse(getHtmlContent()).text().replaceAll("\\s+", " ");
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

	protected String getStringFromReader(Reader reader) throws IOException {
		StringBuilder sb = new StringBuilder();
		int ch;
		while((ch = reader.read()) >= 0) {
			sb.append((char) ch);
		}
		return sb.toString();
	}

	public String getAbstractContent() throws IOException {
		return getAbstractContent(365);
	}

	public String getAbstractContent(int limitWords) throws IOException {
		String content = getTextContent();
		String rearText = "...";
		if (content.length() > limitWords) {
			content = content.substring(0, limitWords - rearText.length());
			content += rearText;
		}
		return content;
	}

}
