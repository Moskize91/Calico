package com.taozeyu.calico.resource;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class ResourceManager {

	private final File sourceDir;
	private final ResourceManager rootResourceManager;

	private ResourceManager[] dirs = null;
	
	private static final String ResourceTemplateExtensionNames[] = new String[] {
		"md", "html", "htm"
	};
	
	public ResourceManager(File sourceDir) {
		this.sourceDir = sourceDir;
		this.rootResourceManager = this;
	}

	private ResourceManager(File sourceDir, ResourceManager rootResourceManager) {
		this.sourceDir = sourceDir;
		this.rootResourceManager = rootResourceManager;
	}

	public AbstractPageResource page(String path) {
		File pageFile = getPageFile(path);
		if(pageFile == null) {
			return null;
		}
		String absolutePath = toAbsolutePath(path);
		switch(getExtensionName(pageFile)) {
			case "md":
				return new MarkdownPageResource(pageFile, absolutePath);

			case "html":
				return new HtmlPageResource(pageFile, absolutePath);

			case "htm":
				return new HtmlPageResource(pageFile, absolutePath);

			default:
				return null;
		}
	}

	private boolean isAbsolutePath(String path) {
		return path.startsWith("/");
	}

	private String toAbsolutePath(String relativePath) {
		if (isAbsolutePath(relativePath)) {
			return relativePath;
		} else {
			String resourcePath = this.sourceDir.getAbsolutePath().substring(
					rootResourceManager.sourceDir.getAbsolutePath().length()
			);
			resourcePath = resourcePath.replaceAll("(^/|/$)", "");
			relativePath = relativePath.replaceAll("(^/|/$)", "");
			return "/"+ resourcePath + "/" + relativePath;
		}
	}

	private File getPageFile(String path) {
		if (isAbsolutePath(path)) {
			return rootResourceManager.getPageFile(path.substring(1));
		} else {
			path = clearExtensionName(path);
			File pageFile = null;
			for(String extensionName:ResourceTemplateExtensionNames) {
				File file = new File(sourceDir.getPath(), path + "." + extensionName);
				if(file.exists() && file.isFile()) {
					pageFile = file;
				}
			}
			return pageFile;
		}
	}

	private String clearExtensionName(String path) {
		return path.replaceAll("\\.(\\w|\\-)+$", "");
	}
	
	private String getExtensionName(File pageFile) {
		String name = pageFile.getName();
		return name.substring(name.lastIndexOf('.') + 1);
	}
	
	public ResourceManager dir(String path) {
		if (isAbsolutePath(path)) {
			return rootResourceManager.dir(path.substring(1));
		} else {
			File dir = new File(sourceDir.getPath(), path);
			if(!dir.exists() && !dir.isDirectory()) {
				throw new ResourceException("directory not found '"+ path +"'.");
			}
			return new ResourceManager(dir, this);
		}
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
				dirsList.add(new ResourceManager(file, this));
			}
		}
		return dirsList.toArray(new ResourceManager[dirsList.size()]);
	}

	public AbstractPageResource[] pages() {
		List<AbstractPageResource> pagesList = new LinkedList<AbstractPageResource>();
		for(File file:sourceDir.listFiles()) {
			if(!file.isDirectory()) {
				AbstractPageResource page = page(file.getName());
				// if page's extend name can't recognize, it should ignore.
				if (page != null) {
					pagesList.add(page);
				}
			}
		}
		return pagesList.toArray(new AbstractPageResource[pagesList.size()]);
	}

	public AbstractPageResource[] allPages() {
		List<AbstractPageResource> pagesList = new LinkedList<AbstractPageResource>();
		for (ResourceManager resourceManager : dirs()) {
			for (AbstractPageResource page : resourceManager.pages()) {
				pagesList.add(page);
			}
		}
		return pagesList.toArray(new AbstractPageResource[pagesList.size()]);
	}
}
