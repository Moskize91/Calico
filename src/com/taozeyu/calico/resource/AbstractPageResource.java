package com.taozeyu.calico.resource;

import org.jsoup.Jsoup;

import java.io.File;
import java.io.IOException;
import java.io.Reader;

public abstract class AbstractPageResource extends AbstractResource<String> {

	AbstractPageResource(File resourceFile, String resourcePath) {
		super(resourceFile, resourcePath);
	}

	public String getHtmlContent() throws IOException {
		return getContent();
	}

	public String getTextContent() throws IOException {
		return Jsoup.parse(getHtmlContent()).text().replaceAll("\\s+", " ");
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
