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
				//contentBuilder �Ỻ��ĩβ����� '--'����Ϊ�ڶ�����3��'-'֮ǰ�Ҳ�֪����������Ǹ�������'---'��
				content = subStringLast2Char(contentBuilder.toString());
				return;
			}
		}
		if(!isReadingContinuousBarSign()) {
			/* 1����ǰû���ڶ� '---' ˵�������Դ�ļ�����û�� Head�����������Ӧ���׳��쳣����Ӧ����Ϊ content Ϊ�ղ��������ء�
			 * 2��û���ڶ� '---' ʱ�ߵ���һ��˵�� Head û�����ļ��ͽ����ˣ�˵����Դ�ļ���©������� '---'������һ�ָ�ʽ����
			 * 3������������� '---'�� Head�������� '---' ��������������ͨ�� finish == true Ȼ�� return �ˡ���ʱ���̲����ߵ���һ���� */
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
				finish = true;//�ļ���ʼ���� "---"����������ļ�ͷ��û�а������ԣ����û��Ҫ����ֱ�ӽ�����
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
