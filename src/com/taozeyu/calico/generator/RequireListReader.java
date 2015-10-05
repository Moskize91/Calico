package com.taozeyu.calico.generator;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.regex.Pattern;

import com.taozeyu.calico.GlobalConifg;


class RequireListReader extends AllowFillReader {

	private static final int BufferedSize = 1024;
	private static final Pattern RequirePattern = Pattern.compile("^/?(\\w|\\-|\\.)+(/(\\w|\\-|\\.)+)*$");
	
	private final Reader reader;
	private final RequireListReader parent;
	private final File readFile;
	private final File routeDir;
	
	private RequireListReader nextReader = null;
	private RequireListReader yieldReader = null;
	
	RequireListReader(File readFile, File routeDir) throws IOException {
		this(null, readFile, routeDir);
	}

	void setYieldReader(RequireListReader reader) {
		yieldReader = reader;
	}

    RequireListReader getLayoutRequireListReader() throws IOException {
        File layoutFile = getFileByRequireInfo("layout.html");
        if (!requireInfoHasNoError(layoutFile)) {
            layoutFile = getFileByRequireInfo("/layout.html");
        }
        if (requireInfoHasNoError(layoutFile)) {
            return new RequireListReader(null, layoutFile, routeDir);
        }
        return null;
    }

	private RequireListReader(RequireListReader parent, File readFile,  File routeDir) throws IOException {
		this.reader =  createReader(readFile);
		this.parent = parent;
		this.readFile = readFile;
		this.routeDir = routeDir;
	}

	private Reader createReader(File file) throws FileNotFoundException {
		InputStream inputStream = new FileInputStream(file);
		inputStream = new BufferedInputStream(inputStream, BufferedSize);
		return new InputStreamReader(inputStream, GlobalConifg.instance.getCharset());
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
			File requireTemplateFile = getFileByRequireInfo(requireInfo);
			checkRequireFileError(requireTemplateFile);
			nextListReader = new RequireListReader(this, requireTemplateFile, routeDir);
		}
		return nextListReader;
	}

	private void checkRequireInfoFormatError(String requireInfo) {
		if(!RequirePattern.matcher(requireInfo).matches()) {
			throw new TemplateException("require format error '"+ requireInfo + "'.");
		}
	}

    private File getFileByRequireInfo(String requireInfo) {
        File requireTemplateFile;
        if(isAbosultePath(requireInfo)) {
            requireTemplateFile = new File(routeDir, requireInfo);
        } else {
            requireTemplateFile = new File(readFile.getParent(), requireInfo);
        }
        return requireTemplateFile;
    }

    private boolean isAbosultePath(String requireInfo) {
		return requireInfo.startsWith("/");
	}

    private boolean requireInfoHasNoError(File requireTemplateFile) {
        try {
            checkRequireFileError(requireTemplateFile);
            return true;
        } catch (TemplateException e) {
            return false;
        }
    }

    private void checkRequireFileError(File requireTemplateFile) {
		if(!requireTemplateFile.exists() || !requireTemplateFile.isFile()) {
			throw new TemplateException("not found template file '"+ requireTemplateFile.getPath() + "'.");
		}
		if(isFileInRequireList(requireTemplateFile)) {
			throw new TemplateException("found circular reference, file has been required '"+ requireTemplateFile.getPath() + "'.");
		}
	}

	@SuppressWarnings("resource")
	private boolean isFileInRequireList(File targetFile) {
		for(RequireListReader reader = this; reader != null; reader = reader.parent) {
			if(reader.readFile.equals(targetFile)) {
				return true;
			}
		}
		return false;
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
