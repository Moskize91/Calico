package com.taozeyu.calico.generator;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Router {

	private final File routePath;
	
	public Router(File routePath) {
		this.routePath = routePath;
	}
	
	public FileGenerator getFileGenerator(String absolutePath) {
		String targetPath = normalizePath(absolutePath);
		String extensionName = getExtensionName(absolutePath);
		String pathCells[] = targetPath.split("/");
		
		return createFileGenerator(absolutePath, pathCells, extensionName);
	}

	private String getExtensionName(String path) {
		String extensionName;
		Matcher matcher = Pattern.compile("\\.\\w+$").matcher(path);
		if(matcher.find()) {
			extensionName = matcher.group().replace("^\\.", "");
		} else {
			extensionName = "html";
		}
		return extensionName;
	}

	private boolean isFileExist(File path) {
		return new File(routePath.getPath(), path.getPath()).exists();
	}

	private String normalizePath(String absolutePath) {
		return absolutePath.replaceAll("\\\\", "/").replace("(\\.\\w+)?/$", "");
	}
	
	private String getParams(String pathCells[], int startIndex) {
		String params = "";
		for(int i=startIndex; i<pathCells.length; ++i) {
			params += pathCells[i];
			if(i < pathCells.length - 1) {
				params += "/";
			}
		}
		return params;
	}
	
	private FileGenerator createFileGenerator(String absolutePath, String[] pathCells, String extensionName) {
		File dirPath = new File("");
		String params = "";
		for(int i=0; i<pathCells.length; ++i) {
			String pathCell = pathCells[i];
			File path = new File(dirPath, pathCell);
			if(!isFileExist(path)) {
				params = getParams(pathCells, i);
				break;
			}
			if(!path.isDirectory()) {
				throw new RouteException("'" + path.getPath() + "' is not directory.");
			}
		}
		File templatePath = getTemplatePath(dirPath, extensionName);
		if(!templatePath.exists() || !templatePath.isFile()) {
			throw new RouteException("can't find template '"+ templatePath.getPath() +"'.");
		} 
		return new FileGenerator(new File(absolutePath), templatePath, params);
	}

	private File getTemplatePath(File dirPath, String extensionName) {
		return new File(routePath.getPath(), dirPath.getPath() + "." + extensionName);
	}
}
