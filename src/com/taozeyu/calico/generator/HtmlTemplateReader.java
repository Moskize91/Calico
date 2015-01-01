package com.taozeyu.calico.generator;

import java.io.IOException;
import java.io.Reader;

class HtmlTemplateReader extends AllowFillReader {

	private static final String PrintHead = "print('";
	private static final String PrintTail = "');\n";
	
	private final StringEscapeReader reader;
	private State state = State.Content;
	
	private boolean hasPrintTail = false;
	
	private static enum State {
		Content, InvokeScript, PrintScript;
	}
	
	HtmlTemplateReader(Reader reader) {
		this.reader = new StringEscapeReader(reader);
		this.reader.fillContent(PrintHead);
		this.reader.setEscapeFlag(true);
	}
	
	@Override
	protected int readOneChar() throws IOException {
		int ch = reader.read();
		if(ch < 0) {
			if(!hasPrintTail) {
				if(state != State.Content) {
					throw new IOException();
				}
				reader.setEscapeFlag(false);
				reader.fillContent(PrintTail);
				
				hasPrintTail = true;
			}
			return ch;
		}
		switch(state) {
		case Content:
			ch = handleCharWhenIsContent(ch);
			break;
			
		case InvokeScript:
			ch = handleCharWhenIsInvokeScript(ch);
			break;
			
		case PrintScript:
			break;
		}
		return ch;
	}

	private int handleCharWhenIsContent(int ch) throws IOException {
		if(ch == '<' && (ch = reader.read()) >= 0) {
			if(ch == '%' && (ch = reader.read()) >= 0) {
				reader.setEscapeFlag(false);
				reader.fillContent(PrintTail);
				if(ch == '=') {
					state = State.PrintScript;
				} else {
					state = State.InvokeScript;
					reader.fillChar((char) ch);
				}
				ch = reader.read();
				
			} else {
				reader.fillChar((char) ch);
				ch = '<';
			}
		}
		return ch;
	}
	
	private int handleCharWhenIsInvokeScript(int ch) throws IOException {
		if(ch == '%' && (ch = reader.read()) >= 0) {
			if(ch == '>') {
				reader.fillContent(PrintHead);
				reader.setEscapeFlag(true);
				state = State.Content;
				ch = reader.read();
				
			} else {
				reader.fillChar((char) ch);
				ch = '%';
			}
		}
		return ch;
	}
	
	@Override
	public void close() throws IOException {
		reader.close();
	}

}
