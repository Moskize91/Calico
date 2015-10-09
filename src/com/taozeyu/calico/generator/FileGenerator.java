package com.taozeyu.calico.generator;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

import javax.script.ScriptException;

import com.taozeyu.calico.GlobalConfig;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class FileGenerator {

	private final PageService pageService;

	private final File absolutePath;
	
	FileGenerator(PageService pageService, File absolutePath) {
		this.pageService = pageService;
		this.absolutePath = absolutePath;
	}

	public List<String> generateAndGetPageLinkList(File targetDir) throws IOException, ScriptException {
		generateTargetFile(targetDir);
		return createPageLinkList(targetDir);
	}

	private void generateTargetFile(File targetDir) throws ScriptException, IOException {

		boolean autoFlush = false;
		PrintStream printStream = new PrintStream(
				getTargetFileOutputStream(targetDir), autoFlush,
				GlobalConfig.instance().getCharset().name());

		try {
			pageService.requestPage(printStream);

		} finally {
			printStream.flush();
			printStream.close();
		}
	}

	private OutputStream getTargetFileOutputStream(File targetDir) throws FileNotFoundException {
		File targetFile = getTargetFile(targetDir);
		targetFile.getParentFile().mkdirs();
		return new FileOutputStream(targetFile);
	}

	private File getTargetFile(File targetDir) {
		return new File(targetDir, absolutePath.getPath() + ".html");
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
		return Jsoup.parse(targetFile, charsetName);
	}
}
