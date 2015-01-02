package com.taozeyu.calico.resource;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class ResourceFileWithHead {

	private static final Pattern HeadLinePattern = Pattern.compile("^(\\w|\\-)+\\s*:\\w*(\\w|\\-)+$");
	private static final int ReadAttributeBufferedSize = 128;
	
	private final File resourceFile;
	
	private Map<String, String> attributeMap = null;
	
	ResourceFileWithHead(File resourceFile) {
		this.resourceFile = resourceFile;
	}

	public Reader createResourceFileReaderBuyJumpOverHead() throws IOException {
		Reader reader = createResourceFileReader();
		jumpOverHead(reader);
		return reader;
	}

	private void jumpOverHead(Reader reader) throws IOException {
		new ResourceHeadContentReader(reader, resourceFile.getPath()).read();
	}
	
	public File getResourceFile() {
		return resourceFile;
	}
	
	public boolean containsName(String name) throws IOException {
		return getAttributeMapAndReadIfNotExist().containsKey(name);
	}
	
	public String getAttribute(String name) throws IOException {
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
		return new InputStreamReader(inputStream);
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
		return line.split("\\s*\\:\\s*");
	}

}
