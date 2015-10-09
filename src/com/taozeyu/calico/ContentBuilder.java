package com.taozeyu.calico;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.regex.Pattern;

import javax.script.ScriptException;

import com.taozeyu.calico.generator.FileGenerator;
import com.taozeyu.calico.generator.Router;
import com.taozeyu.calico.util.PathUtil;

class ContentBuilder {

	private static final Pattern UrlWithDomain = Pattern.compile("^(http|https)://(\\w|\\-)+(\\.(\\w+|\\-))*(:\\d+)?");
	
	private final Router router;
	private final File targetDir;
	
	private final Queue<String> pathQueue = new LinkedList<String>();
	private final Set<String> handledPathSet = new HashSet<String>();
	
	ContentBuilder(Router router, File targetDir) {
		this.router = router;
		this.targetDir = targetDir;
	}
	
	void buildFromRootFile() throws IOException, ScriptException {
		System.out.println("");
		System.out.println("Generate html pages.");
		findPathAndTryAddToQueue(Router.RootPath);
		while(!isQueueEmpty()) {
			String path = getPathFromQueue();
			System.out.println("\tgenerate path "+ path);
			FileGenerator generator = router.getFileGenerator(path);
			List<String> linkList = generator.generateAndGetPageLinkList(targetDir);
			for(String linkPath: linkList) {
				handleLinkPath(path, linkPath);
			}
		}
	}

	private void handleLinkPath(String currentPagePath, String linkPath) {
		if(isWithoutDomain(linkPath)) {
			String absolutePath = toAbsolutePath(currentPagePath, linkPath);
			findPathAndTryAddToQueue(absolutePath);
		}
	}

	private boolean isWithoutDomain(String linkPath) {
		return !UrlWithDomain.matcher(linkPath).find();
	}

	private String clearTailFileName(String currentPagePath) {
		return currentPagePath.replaceAll("(\\w|\\-)+(\\.(\\w|\\-)+)*/?$", "");
	}

	private void findPathAndTryAddToQueue(String path) {
		if(!handledPathSet.contains(path)) {
			pathQueue.add(path);
			handledPathSet.add(path);
		}
	}

	private String toAbsolutePath(String currentPagePath, String linkPath) {
		if(!PathUtil.isAbsolutePath(linkPath)) {
			String parentPath = clearTailFileName(currentPagePath);
			return parentPath + linkPath;
		}
		return linkPath;
	}

	private boolean isQueueEmpty() {
		return pathQueue.isEmpty();
	}
	
	private String getPathFromQueue() {
		return pathQueue.remove();
	}
}
