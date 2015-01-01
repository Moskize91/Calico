package com.taozeyu.calico.generator;

import java.io.IOException;
import java.io.Reader;

abstract class AllowFillReader extends Reader {

	private String fillContent = null;
	private int nextIndex = 0;
	
	protected abstract int readOneChar() throws IOException ;
	
	protected void fillContent(String content) {
		if(fillContent != null) {
			fillContent += content;
		} else {
			fillContent = content;
			nextIndex = 0;
		}
	}
	
	protected void fillChar(char ch) {
		fillContent(String.valueOf(ch));
	}
	
	private int readOneCharConsiderFillContent() throws IOException {
		if(fillContent == null) {
			int ch = readOneChar();
			return ch;
			
		} else {
			char ch = fillContent.charAt(nextIndex);
			nextIndex++;
			if(nextIndex >= fillContent.length()) {
				fillContent = null;
			}
			return ch;
		}
	}
	
	@Override
	public int read(char[] cbuf, int off, int len) throws IOException {
		int count = 0;
		for(int i=0; i<len; ++i) {
			int ch = readOneCharConsiderFillContent();
			if(ch < 0) {
				count = -1;
				break;
			}
			cbuf[off + i] = (char) ch;
			count++;
		}
		return count;
	}
}
