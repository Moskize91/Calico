package com.taozeyu.calico.generator;

import java.io.*;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.taozeyu.calico.GlobalConfig;
import com.taozeyu.calico.javascript_helper.JavaScriptLoader;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.taozeyu.calico.resource.ResourceManager;

public class FileGenerator {

	private static final int BufferedSize = 1024;
	private static final Charset LibraryCharset = Charset.forName("UTF-8");
	
	private final ResourceManager resource;
	
	private final File absolutePath;
	private final File templatePath;
	private final File routeDir;
	private final String params;
	
	FileGenerator(ResourceManager resource, File absolutePath, File templatePath, File routeDir, String params) {
		this.resource = resource;
		this.absolutePath = absolutePath;
		this.templatePath = templatePath;
		this.routeDir = routeDir;
		this.params = params;
	}

	public List<String> generateAndGetPageLinkList(File targetDir) throws IOException, ScriptException {
	    ScriptEngine engine = createScriptEngine();
		generateTargetFile(targetDir, engine);
		return createPageLinkList(targetDir);
	}

	private ScriptEngine createScriptEngine() throws IOException {
	    ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
		engine.put("__resource", resource);
	    engine.put("params", params);
	    try {
			new JavaScriptLoader().loadSystemJavaScriptLib(engine);
	    } catch (ScriptException e) {
	    	e.printStackTrace();
	    	System.exit(1); //can only exit when JS lib throws error.
	    }
		return engine;
	}

	private void generateTargetFile(File targetDir, ScriptEngine jse) throws ScriptException, IOException {

		boolean autoFlush = false;
		PrintStream printStream = new PrintStream(
				getTargetFileOutputStream(targetDir), autoFlush,
				GlobalConfig.instance().getCharset().name());
		Reader reader = getTemplateReader();

		try {
			setPrintStreamToEngin(jse, printStream);
			jse.eval(getFileContentFromReader(reader));
		    
		} finally {
			printStream.flush();
			printStream.close();
			reader.close();
		}
	}

	private void setPrintStreamToEngin(ScriptEngine jse, PrintStream printStream) throws ScriptException {
		jse.put("__printStream", printStream);
		jse.eval("Output = __printStream;");
		jse.eval("__printStream = undefined;");
	}

	// Nashorn engine would omit some content if read from a long stream.
	// But all will be fine if buffered them before reading.
	// I don't know why.
	private String getFileContentFromReader(Reader reader) throws IOException {
	    StringBuilder sb = new StringBuilder();
	    for(int ch = reader.read(); ch >= 0; ch = reader.read()) {
	    	sb.append((char) ch);
	    }
	    return sb.toString();
	}
	
	private OutputStream getTargetFileOutputStream(File targetDir) throws FileNotFoundException {
		File targetFile = getTargetFile(targetDir);
		targetFile.getParentFile().mkdirs();
		return new FileOutputStream(targetFile);
	}

	private File getTargetFile(File targetDir) {
		return new File(targetDir, absolutePath.getPath() + ".html");
	}

	private Reader getTemplateReader() throws IOException {
		RequireListReader templateReader = new RequireListReader(templatePath, routeDir);
		RequireListReader layoutReader = templateReader.getLayoutRequireListReader();
		if (layoutReader != null) {
			layoutReader.setYieldReader(templateReader);
			return new HtmlTemplateReader(layoutReader);
		} else {
			return new HtmlTemplateReader(templateReader);
		}
	}

	private void loadScriptLibrary(ScriptEngine engine) throws ScriptException, IOException {
		Reader reader = getReaderFromFile(LibraryCharset);
		try {
			engine.getContext().setWriter(new OutputStreamWriter(System.out, LibraryCharset));
			engine.getContext().setErrorWriter(new OutputStreamWriter(System.err, LibraryCharset));
			engine.eval(reader);
		} finally {
			reader.close();
		}
	}

	private Reader getReaderFromFile(Charset charset) throws FileNotFoundException {
		InputStream inputStream = new FileInputStream(templatePath);
		inputStream = new BufferedInputStream(inputStream, BufferedSize);
		return  new InputStreamReader(inputStream, charset);
	}

	private List<String> createPageLinkList(File targetDir) throws IOException {
		Document doc = getDocumentFromTargetFile(targetDir);
		List<String> pageLinkList = new LinkedList<String>();

		for(Element link:doc.select("a[href~=^.+\\.html?$]")) {
			pageLinkList.add(link.attr("href"));
		}
		return pageLinkList;
	}

	private Document getDocumentFromTargetFile(File targetDir) throws IOException {
		File targetFile = getTargetFile(targetDir);
		String charsetName = GlobalConfig.instance().getCharset().name();
		Document doc = Jsoup.parse(targetFile, charsetName);
		return doc;
	}
}
