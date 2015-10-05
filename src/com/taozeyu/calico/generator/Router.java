package com.taozeyu.calico.generator;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.taozeyu.calico.resource.ResourceManager;

public class Router {

	public static final String RootPath = "/";

	private final ResourceManager resource;
	private final File routeDir;
	private final String rootMapToPath;
	
	public Router(ResourceManager resource, File routeDir, String rootMapToPath) {
		this.resource = resource;
		this.routeDir = routeDir;
		this.rootMapToPath = rootMapToPath;
	}
	
	public FileGenerator getFileGenerator(String absolutePath) {

		String targetPath = normalizePath(absolutePath);

		if(targetPath.equals(RootPath)) {
			targetPath = rootMapToPath;
		}
		return useAbsoluteTemplateOrUseParams(targetPath);
	}

	private FileGenerator useAbsoluteTemplateOrUseParams(String targetPath) {
		File targetPathTemplateFile = new File(routeDir, targetPath);
		if(targetPathTemplateFile.exists()) {
			String params = "";
			return new FileGenerator(resource, new File(targetPath), targetPathTemplateFile, routeDir, params);
			
		} else {
			String extensionName = getExtensionName(targetPath);
			String pathCells[] = clearHeadTailSlash(targetPath).split("/");
			return createFileGenerator(targetPath, pathCells, extensionName);
		}
	}

	private String getExtensionName(String path) {
		String extensionName;
		Matcher matcher = Pattern.compile("\\.(\\w|\\-)+$").matcher(path);
		if(matcher.find()) {
			extensionName = matcher.group().replaceAll("^\\.", "");
		} else {
			extensionName = "html";
		}
		return extensionName;
	}

	private String clearHeadTailSlash(String path) {
		return path.replaceAll("^/", "").replaceAll("/$", "");
	}

	private String normalizePath(String absolutePath) {
		return absolutePath.replaceAll("\\\\", "/").replaceAll("(\\.(\\w|\\-)+/?)?$", "");
	}
	
	private FileGenerator createFileGenerator(String absolutePath, String[] pathCells, String extensionName) {
		
		int endOfExistDirIndex = findEndOfExistDirIndex(pathCells, extensionName);
		String path = getTemplateDirPath(pathCells, endOfExistDirIndex);
		File templatePath = getTemplatePath(path, extensionName);
		String params = selectParamsFromPath(pathCells, endOfExistDirIndex + 1);
		checkTemplatePath(templatePath); 
		return new FileGenerator(resource, new File(absolutePath), templatePath, routeDir, params);
	}

	private String getTemplateDirPath(String[] pathCells, int endOfExistDirIndex) {
		String path = "";
		for(int i=0; i<endOfExistDirIndex + 1; ++i) {
			path += pathCells[i];
			if(i < endOfExistDirIndex) {
				path += "/";
			}
		}
		return path;
	}

	private int findEndOfExistDirIndex(String[] pathCells, String extensionName) {
		int endOfExistDirIndex = -1;
		File path = new File("");
		for(int i=0; i<pathCells.length; ++i) {
			endOfExistDirIndex = i;
			String pathCell = pathCells[i] + "." + extensionName;
			path = new File(path, pathCell);
			if(isFileExist(path)) {
				break;
			}
		}
		return endOfExistDirIndex;
	}

	private boolean isFileExist(File path) {
		return new File(routeDir.getPath(), path.getPath()).exists();
	}

	private String selectParamsFromPath(String pathCells[], int startIndex) {
		String params = "";
		for(int i=startIndex; i<pathCells.length; ++i) {
			params += pathCells[i];
			if(i < pathCells.length - 1) {
				params += "/";
			}
		}
		return params;
	}

	private void checkTemplatePath(File templatePath) {
		if(!templatePath.exists() || !templatePath.isFile()) {
			throw new RouteException("can't find template '"+ templatePath.getPath() +"'.");
		}
	}

	private File getTemplatePath(String dirPath, String extensionName) {
		return new File(routeDir.getPath(), dirPath + "." + extensionName);
	}
}
