package com.taozeyu.calico.resource;

import java.io.IOException;
import java.io.Reader;

class ResourceHeadContentReader {

	private static final int ContinuousBarSignNum = 3;
	
	private final Reader reader;
	private final String resourceName;
	private final StringBuilder contentBuilder;
	
	private String content = "";
	private boolean finishReadMark;
	
	private int readContinuousBarSignCount = 0;
	private boolean hasReadFirstContinuousTreeBarSign = false;
	
	private State state = State.Init;
	
	private static enum State {
		Init, BarSign, HeadContent,
	}
	
	ResourceHeadContentReader(Reader reader, String resourceName) {
		this.reader = reader;
		this.resourceName = resourceName;
		this.contentBuilder = new StringBuilder();
	}
	
	void read() throws IOException {
		for(int ch = reader.read(); ch > 0; ch = reader.read()) {
			boolean finish = readOneCharAndCheckIsFinish((char) ch);
			if(finish) {
				//contentBuilder �Ỻ��ĩβ����� '---'�����Ƕ���ģ���˱���ɾ����
				content = subStringLast3Char(contentBuilder.toString());
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

	private String subStringLast3Char(String str) {
		return str.replaceAll("(^\\-{3}|\\-{3}$)", "");
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
		finishReadMark = false;
		switch(state) {
		case Init:
			handleWhenInit(ch);
			break;
			
		case BarSign:
			handleWhenIsBarSign(ch);
			break;
			
		case HeadContent:
			handleWhenIsHeadContent(ch);
			break;
		}
		contentBuilder.append((char) ch);
		
		return finishReadMark;
	}

	private void handleWhenInit(char ch) {
		if(ch == '-') {
			readContinuousBarSignCount++;
			state = State.BarSign;
		} else {
			finishReadMark = true;//�ļ���ʼ���� "---"����������ļ�ͷ��û�а������ԣ����û��Ҫ����ֱ�ӽ�����
		}
	}

	private void handleWhenIsBarSign(char ch) {
		if(ch == '-') {
			readContinuousBarSignCount++;
			if(readContinuousBarSignCount >= ContinuousBarSignNum) {
				if(hasReadFirstContinuousTreeBarSign) {
					finishReadMark = true;
				} else {
					hasReadFirstContinuousTreeBarSign = true;
				}
			}
		} else {
			readContinuousBarSignCount = 0;
			state = State.HeadContent;
		}
	}

	private void handleWhenIsHeadContent(char ch) {
		if(ch == '-') {
			readContinuousBarSignCount++;
			state = State.BarSign;
		}
	}

	private boolean isReadingContinuousBarSign() {
		return readContinuousBarSignCount > 0;
	}
}
