package com.taozeyu.calico.resource;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResourceFileWithMetaData {

	private static final Pattern HeadLinePattern = Pattern.compile("^(\\w|\\-)+\\s*:\\s*.+\\s*$");
	private static final int ReadAttributeBufferedSize = 128;
	
	protected final File resourceFile;
	
	private Map<String, String> attributeMap = null;
	
	ResourceFileWithMetaData(File resourceFile) {
		this.resourceFile = resourceFile;
	}

	public Reader createResourceFileReaderByJumpOverHead() throws IOException {
		Reader reader = createResourceFileReader();
		ResourceHeadContentReader headContentReader = new ResourceHeadContentReader(reader, resourceFile.getPath());
		Character firstChar = headContentReader.read();
		if (firstChar != null) {
			reader = new ReaderWithOneChar(reader, firstChar);
		}
		return reader;
	}

	public File getResourceFile() {
		return resourceFile;
	}
	
	public boolean containsName(String name) throws IOException {
		return getAttributeMapAndReadIfNotExist().containsKey(name);
	}
	
	public String get(String name) throws IOException {
		return getAttributeMapAndReadIfNotExist().get(name);
	}
	
	public Iterable<String> nameSet() throws IOException {
		return getAttributeMapAndReadIfNotExist().keySet();
	}
	
	public int getAttributesSize() throws IOException {
		return getAttributeMapAndReadIfNotExist().size();
	}
	
	private Map<String, String> getAttributeMapAndReadIfNotExist() throws IOException {
		if(attributeMap == null) {
			attributeMap = readAttributes();
		}
		return attributeMap;
	}
	
	private Map<String, String> readAttributes() throws IOException {
		ResourceHeadContentReader headReader = new ResourceHeadContentReader(createResourceFileReader(), resourceFile.getPath());
		try {
			Map<String, String> attributeMap = new HashMap<String, String>();
			headReader.read();
			if(headReader.hasAnyContent()) {
				String content = headReader.getContent();
				for(String line:spliteIntoLineAndClearEmptyLine(content)) {
					collectLineMessageIntoAttributes(attributeMap, line);
				}
			}
			return attributeMap;
			
		}finally {
			headReader.close();
		}
	}
	
	private Reader createResourceFileReader() throws FileNotFoundException {
		InputStream inputStream = new FileInputStream(resourceFile);
		inputStream = new BufferedInputStream(inputStream, ReadAttributeBufferedSize);
		return new InputStreamReader(inputStream, Charset.forName("UTF-8"));
	}
	
	private void collectLineMessageIntoAttributes(Map<String, String> attributeMap, String line) {
		checkLineAndThrowErrorIfIllegal(line);
		String[] cells = splitLineIntoKeyValueArray(line);
		String key = cells[0];
		String value = cells[1];
		attributeMap.put(key, value);
	}

	private List<String> spliteIntoLineAndClearEmptyLine(String content) {
		String lines[] = content.split("\\n");
		List<String> linesList = new ArrayList<String>(lines.length);
		for(int i=0; i<lines.length; ++i) {
			String line = lines[i].trim();
			if(!"".equals(line)) {
				linesList.add(line);
			}
		}
		return linesList;
	}
	
	private void checkLineAndThrowErrorIfIllegal(String line) {
		if(!HeadLinePattern.matcher(line).matches()) {
			throw new ResourceException("illegal content '"+ line+ "' from file "+ resourceFile.getPath());
		}
	}

	private String[] splitLineIntoKeyValueArray(String line) {
		Matcher matcher = Pattern.compile("\\s*\\:\\s*").matcher(line);
		if (!matcher.find()) {
			return new String[] {line, ""};
		} else {
			return new String[] {
					line.substring(0, matcher.start()).trim(),
					line.substring(matcher.end()).trim(),
			};
		}
	}

	@Override
	public String toString() {
		return this.getClass().getName() + ":" + resourceFile;
	}
}
