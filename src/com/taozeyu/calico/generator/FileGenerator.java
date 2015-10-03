package com.taozeyu.calico.generator;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.taozeyu.calico.GlobalConifg;
import com.taozeyu.calico.resource.ResourceManager;

public class FileGenerator {

	private static final int BufferedSize = 1024;
	
	private static final String LiberaryPath = "javascript";
	private static final Charset LiberaryCharset = Charset.forName("UTF-8");
	
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
	    engine.put("params", params);
	    try {
			loadEveryScriptLiberary(engine);
	    } catch (ScriptException e) {
	    	e.printStackTrace();
	    	System.exit(1); //can only exit when JS lib throws error.
	    }
		return engine;
	}

	private void generateTargetFile(File targetDir, ScriptEngine jse) throws FileNotFoundException, ScriptException, IOException {
		Writer writer = getTargetFileWriter(targetDir);
		Reader reader = getTemplateReader();
		
		try {
			jse.put("R", resource);
		    jse.getContext().setWriter(writer);
		    jse.eval(getFileContentFromReader(reader));
		    
		} finally {
			writer.flush();
			writer.close();
			reader.close();
		}
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
	
	private Writer getTargetFileWriter(File targetDir) throws FileNotFoundException {
		File targetFile = getTargetFile(targetDir);
		targetFile.getParentFile().mkdirs();
		
		Writer writer = new OutputStreamWriter(
				new FileOutputStream(targetFile),
				GlobalConifg.instance.getCharset()
		);
		return writer;
	}

	private File getTargetFile(File targetDir) {
		return new File(targetDir, absolutePath.getPath());
	}

	private Reader getTemplateReader() throws IOException {
		Reader reader = new RequireListReader(templatePath, routeDir);
		return new HtmlTemplateReader(reader);
	}

	private void loadEveryScriptLiberary(ScriptEngine engine) throws ScriptException, IOException {
		File liberaryPath = getLiberaryDirPath();
		File[] files = liberaryPath.listFiles();
		for(File file:files) {
			if(isExtensionNameJs(file)) {
				loadScriptLiberary(engine, file);
			}
		}
	}

	private void loadScriptLiberary(ScriptEngine engine, File file) throws ScriptException, IOException {
		Reader reader = getReaderFromFile(file, LiberaryCharset);
		try {
			engine.getContext().setWriter(new OutputStreamWriter(System.out, LiberaryCharset));
			engine.getContext().setErrorWriter(new OutputStreamWriter(System.err, LiberaryCharset));
			engine.eval(reader);
		} finally {
			reader.close();
		}
	}

	private File getLiberaryDirPath() {
		File liberaryPath = new File(System.getProperty("user.dir"), LiberaryPath);
		
		if(!liberaryPath.exists() || !liberaryPath.isDirectory()) {
			throw new TemplateException("can't find javascript liberary '"+ liberaryPath.getPath() +"'.");
		}
		return liberaryPath;
	}

	private boolean isExtensionNameJs(File file) {
		return file.getName().matches(".+\\.(?i)js$");
	}
	
	private Reader getReaderFromFile(File file, Charset charset) throws FileNotFoundException {
		InputStream inputStream = new FileInputStream(templatePath);
		inputStream = new BufferedInputStream(inputStream, BufferedSize);
		return  new InputStreamReader(inputStream, charset);
	}

	private List<String> createPageLinkList(File targetDir) throws IOException {
		Document doc = getDocumentFromTargetFIle(targetDir);
		List<String> pageLinkList = new LinkedList<String>();
		
		for(Element link:doc.select("a[href~=^.+\\.html?$]")) {
			pageLinkList.add(link.attr("href"));
		}
		return pageLinkList;
	}

	private Document getDocumentFromTargetFIle(File targetDir) throws IOException {
		File targetFile = getTargetFile(targetDir);
		String charsetName = GlobalConifg.instance.getCharset().name();
		Document doc = Jsoup.parse(targetFile, charsetName);
		return doc;
	}
}
