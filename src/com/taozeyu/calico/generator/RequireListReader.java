package com.taozeyu.calico.generator;

import com.taozeyu.calico.EntityPathContext;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.regex.Pattern;

class RequireListReader extends AllowFillReader {

	private static final int BufferedSize = 1024;
	private static final Pattern RequirePattern = Pattern.compile("^/?(\\w|-|\\.)+(/(\\w|-|\\.)+)*$");
	
	private final Reader reader;
	private final RequireListReader parent;
	private final EntityPathContext entityPathContext;
	private final String requirePagePath;

	private RequireListReader nextReader = null;
	private RequireListReader yieldReader = null;
	
	RequireListReader(EntityPathContext entityPathContext,
					  String requirePagePath) throws IOException {
		this(null, entityPathContext, requirePagePath);
	}

	void setYieldReader(RequireListReader reader) {
		yieldReader = reader;
	}

    RequireListReader getLayoutRequireListReader() throws IOException {
    	EntityPathContext context = entityPathContext.findContext(requirePagePath);
		String layoutPath = "layout.html";
		if (!context.entityExist(layoutPath) ||
			isRequirePathInRequireList(context.absolutionPathOfThis(layoutPath))) {
			context = entityPathContext.findContext("/");
			layoutPath = "/layout.html";
		}
		if (!context.entityExist(layoutPath) ||
			isRequirePathInRequireList(context.absolutionPathOfThis(layoutPath))) {
			return null;
		}
        return new RequireListReader(context, layoutPath);
    }

	private RequireListReader(RequireListReader parent,
							  EntityPathContext entityPathContext,
							  String requirePagePath) throws IOException {
		this.parent = parent;
		this.entityPathContext = entityPathContext;
		this.requirePagePath = requirePagePath;
		this.reader = createReader(entityPathContext, requirePagePath);
	}

	private Reader createReader(EntityPathContext entityPathContext,
								String requirePagePath) throws FileNotFoundException {
		InputStream inputStream = entityPathContext.inputStreamOfFile(requirePagePath);
		inputStream = new BufferedInputStream(inputStream, BufferedSize);
		return new InputStreamReader(inputStream, Charset.forName("UTF-8"));
	}
	
	@Override
	protected int readOneChar() throws IOException {
		if(nextReader != null) {
			return tryReadFromNextReader();
		} else {
			return readFromSelfAndCheckRequire();
		}
	}

	private int tryReadFromNextReader() throws IOException {
		int ch;
		ch = nextReader.read();
		if(ch < 0) {
			clearNextReader();
			ch = reader.read();
		}
		return ch;
	}

	private int readFromSelfAndCheckRequire() throws IOException {
		int ch = reader.read();
		if(ch < 0) {
			return ch;
		}
		if(ch == '<' && (ch = reader.read()) > 0) {
			if(ch == '%' && (ch = reader.read()) > 0) {
				if(ch == '~') {
					nextReader = createNextListReader();
					ch = tryReadFromNextReader();
				} else {
					fillChar('%');
					fillChar((char) ch);
					ch = '<';
				}
			} else {
				fillChar((char) ch);
				ch = '<';
			}
		}
		return ch;
	}

	private void clearNextReader() throws IOException {
		nextReader.close();
		nextReader = null;
	}

	private String readRequireInfoFromReader() throws IOException {
		StringBuilder sb = new StringBuilder();
		while(true) {
			int ch = readButBanEnd();
			if(ch == '%') {
				ch = readButBanEnd();
				if(ch == '>') {
					break;
				}
				sb.append('%');
			}
			sb.append((char) ch);
		}
		return sb.toString();
	}
	
	private int readButBanEnd() throws IOException {
		int ch = reader.read();
		if(ch < 0) {
			throw new TemplateException("missing '%>' at the end.");
		}
		return ch;
	}

	private RequireListReader createNextListReader() throws IOException {
		RequireListReader nextListReader;
		String requireInfo = readRequireInfoFromReader();
		requireInfo = requireInfo.trim();
		if (requireInfo.equals("yield") && yieldReader != null) {
			nextListReader = yieldReader;
			yieldReader = null;
		} else {
			checkRequireInfoFormatError(requireInfo);
			if (!entityPathContext.entityExist(requireInfo)) {
				throw new TemplateException("template file not found `"+ requireInfo + "`.");
			}
			EntityPathContext.ContextResult result = entityPathContext.findFileAndParentContext(requireInfo);
			nextListReader = new RequireListReader(result.getContext(), result.getFileName());
		}
		return nextListReader;
	}

	private void checkRequireInfoFormatError(String requireInfo) {
		if(!RequirePattern.matcher(requireInfo).matches()) {
			throw new TemplateException("require format error `"+ requireInfo + "`.");
		}
	}

	@SuppressWarnings("resource")
	private boolean isRequirePathInRequireList(String absolutionRequirePath) {
		for(RequireListReader reader = this; reader != null; reader = reader.parent) {
			if (absolutionRequirePath.equals(reader.absolutionRequirePagePath())) {
				return true;
			}
		}
		return false;
	}

	private String absolutionRequirePagePath() {
		return entityPathContext.absolutionPathOfThis(requirePagePath);
	}

	@Override
	public void close() throws IOException {
		reader.close();
		if (yieldReader != null) {
			yieldReader.close();
		}
		if(nextReader != null) {
			clearNextReader();
		}
	}
}
