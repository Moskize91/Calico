package com.taozeyu.calico.resource;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class ResourceManager {

	private final File sourceDir;
	private ResourceManager[] dirs = null;
	
	public ResourceManager(File sourceDir) {
		this.sourceDir = sourceDir;
	}

	public AbstractPageResource page(String path) {
		File pageFile = new File(sourceDir.getPath(), path);
		if(!pageFile.exists() || !pageFile.isFile()) {
			return null;
		}
		switch(getExtensionName(pageFile)) {
		case "md":
			return new MarkdownPageResource(pageFile);
			
		case "html":
			return new HtmlPageResource(pageFile);
			
		case "htm":
			return new HtmlPageResource(pageFile);
			
		default:
			return null;
		}
	}

	private String getExtensionName(File pageFile) {
		String name = pageFile.getName();
		return name.substring(name.lastIndexOf('.') + 1);
	}
	
	public ResourceManager dir(String path) {
		File dir = new File(sourceDir.getPath(), path);
		if(!dir.exists() && !dir.isDirectory()) {
			throw new ResourceException("directory not found '"+ path +"'.");
		}
		return new ResourceManager(dir);
	}
	
	public ResourceManager[] dirs() {
		if(dirs == null) {
			dirs = listAllDirsAndCreateResourceArray();
		}
		return dirs;
	}

	private ResourceManager[] listAllDirsAndCreateResourceArray() {
		List<ResourceManager> dirsList = new LinkedList<ResourceManager>();
		for(File file:sourceDir.listFiles()) {
			if(file.isDirectory()) {
				dirsList.add(new ResourceManager(file));
			}
		}
		return (ResourceManager[]) dirsList.toArray(new ResourceManager[dirsList.size()]);
	}
}
