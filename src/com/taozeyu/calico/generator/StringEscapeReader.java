package com.taozeyu.calico.generator;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

class StringEscapeReader extends AllowFillReader {

	@SuppressWarnings("serial")
	private static Map<Character, String> escapeMap = new HashMap<Character, String>(){{
		put('\n', "n");
		put('\t', "t");
		put('\b', "b");
		put('\\', "\\");
		put('\'', "\'");
		put('\"', "\"");
	}};
	
	private final Reader reader;
	private boolean escapeFlag = false;
	
	StringEscapeReader(Reader reader) {
		this.reader = reader;
	}

	public boolean getEscapeFlag() {
		return escapeFlag;
	}

	public void setEscapeFlag(boolean escapeFlag) {
		this.escapeFlag = escapeFlag;
	}

	@Override
	protected void fillContent(String content) {
		String fillContent;
		if(escapeFlag) {
			fillContent = escapeString(content);
		} else {
			fillContent = content;
		}
		super.fillContent(fillContent);
	}

	private String escapeString(String content) {
		String fillContent;
		fillContent = "";
		for(int i=0; i<content.length(); ++i) {
			char ch = content.charAt(i);
			if(escapeMap.containsKey(ch)) {
				fillContent += "\\" + escapeMap.get(ch);
			} else {
				fillContent += ch;
			}
		}
		return fillContent;
	}

	@Override
	protected int readOneChar() throws IOException {
		int ch = reader.read();
		if(ch < 0) {
			return ch;
		}
		if(escapeFlag) {
			if(escapeMap.containsKey((char) ch)) {
				ch = '\\';
				fillContent(escapeMap.get((char) ch));
			}
		}
		return ch;
	}
	
	@Override
	public void close() throws IOException {
		reader.close();
	}
}
