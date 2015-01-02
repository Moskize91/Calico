package com.taozeyu.calico.resource;

import java.io.IOException;
import java.io.Reader;

class ResourceHeadContentReader {

	private static final int ContinuousBarSignNum = 3;
	
	private final Reader reader;
	private final String resourceName;
	private final StringBuilder contentBuilder;
	
	private String content = "";
	
	private int readContinuousBarSignCount = 0;
	private boolean hasReadFirstContinuousTreeBarSign = false;
	
	ResourceHeadContentReader(Reader reader, String resourceName) {
		this.reader = reader;
		this.resourceName = resourceName;
		this.contentBuilder = new StringBuilder();
	}
	
	void read() throws IOException {
		for(int ch = reader.read(); ch > 0; ch = reader.read()) {
			boolean finish = readOneCharAndCheckIsFinish((char) ch);
			if(finish) {
				//contentBuilder 会缓存末尾额外的 '--'，因为在读到第3个'-'之前我不知道这个东西是个完整的'---'。
				content = subStringLast2Char(contentBuilder.toString());
				return;
			}
		}
		if(!isReadingContinuousBarSign()) {
			/* 1、当前没有在读 '---' 说明这个资源文件可能没有 Head，这种情况不应该抛出异常，而应该视为 content 为空并正常返回。
			 * 2、没有在读 '---' 时走到这一步说明 Head 没读完文件就结束了，说明资源文件遗漏了下面的 '---'，这是一种格式错误。
			 * 3、如果读到两个 '---'（ Head被这两个 '---' 包含起来），则通过 finish == true 然后 return 了。此时流程不会走到这一步。 */
			throw new ResourceException("resource file missing '---' at the end. "+ resourceName);
		}
	}

	private String subStringLast2Char(String str) {
		return str.substring(0, str.length() - 2);
	}
	
	void close() throws IOException {
		reader.close();
	}
	
	String getContent() {
		return content;
	}
	
	boolean hasAnyContent() {
		return hasReadFirstContinuousTreeBarSign;
	}
	
	private boolean readOneCharAndCheckIsFinish(char ch) {
		boolean finish = false;
		
		if(hasReadContinuousThreeBarSignChar(ch)) {
			if(hasReadFirstContinuousTreeBarSign) {
				finish = true;
			} else {
				hasReadFirstContinuousTreeBarSign = true;
			}
		} else {
			if(hasReadFirstContinuousTreeBarSign) {
				contentBuilder.append(ch);
			} else {
				finish = true;//文件起始不是 "---"，表明这个文件头部没有包含属性，因此没必要读，直接结束。
			}
		}
		return finish;
	}

	private boolean hasReadContinuousThreeBarSignChar(char ch) {
		if(ch == '-') {
			readContinuousBarSignCount++;
			if(readContinuousBarSignCount >= ContinuousBarSignNum) {
				readContinuousBarSignCount = 0;
				return true;
			}
		} else {
			readContinuousBarSignCount = 0;
		}
		return false;
	}
	
	private boolean isReadingContinuousBarSign() {
		return readContinuousBarSignCount > 0;
	}
}
