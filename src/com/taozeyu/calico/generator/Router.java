package com.taozeyu.calico.generator;

import java.io.File;
import java.io.InputStream;

import com.taozeyu.calico.EntityPathContext;
import com.taozeyu.calico.RuntimeContext;
import com.taozeyu.calico.resource.ResourceManager;
import com.taozeyu.calico.util.PathUtil;

public class Router {

	public static final String RootPath = "/";
	private static final String[] IndexFilePosibleExtensionNames = new String[]{
			"html", "htm",
	};

	private final RuntimeContext runtimeContext;
	private final EntityPathContext assetEntity;
	private final EntityPathContext pageEntity;
	private final ResourceManager resource;
	private final File routeDir;
	
	public Router(RuntimeContext runtimeContext, ResourceManager resource) {
		this.runtimeContext = runtimeContext;
		this.resource = resource;
		this.routeDir = runtimeContext.getTemplateDirectory();

		File assetEntityModule = new File(
				runtimeContext.getTemplateDirectory(),
				EntityPathContext.EntityType.Asset.getDirectoryName()
		);
		File pageEntityModule = new File(
				runtimeContext.getTemplateDirectory(),
				EntityPathContext.EntityType.Page.getDirectoryName()
		);
		this.assetEntity = new EntityPathContext(
				runtimeContext,
				EntityPathContext.EntityType.Asset,
				EntityPathContext.EntityModule.Template,
				assetEntityModule, "/"
		);
		this.pageEntity = new EntityPathContext(
				runtimeContext,
				EntityPathContext.EntityType.Page,
				EntityPathContext.EntityModule.Template,
				pageEntityModule, "/"
		);
	}

	public boolean existAsset(String relativePath) {
		return assetEntity.entityExist(relativePath);
	}

	public InputStream getAsset(String relativePath) {
		return assetEntity.inputStreamOfFile(relativePath);
	}

	public FileGenerator getFileGenerator(String absolutePath) {

		String targetPath = PathUtil.normalizePathAndCleanExtensionName(absolutePath);
		boolean isRootPage = false;

		if(targetPath.equals(RootPath)) {
			targetPath = runtimeContext.getRootPage();
			isRootPage = true;
		}
		PageService pageService = getPageServiceWithNormalizeTargetPath(targetPath);
		if (pageService == null) {
			throw new RouteException("No template file map to '"+ absolutePath +"'.");
		}
		return new FileGenerator(pageService, new File(targetPath), isRootPage);
	}

	public PageService getPageService(String absolutePath) {

		String targetPath = PathUtil.normalizePathAndCleanExtensionName(absolutePath);

		if(targetPath.equals(RootPath)) {
			targetPath = runtimeContext.getRootPage();
		}
		return getPageServiceWithNormalizeTargetPath(targetPath);
	}

	private PageService getPageServiceWithNormalizeTargetPath(String absolutePath) {
		if (pageEntity.entityExist(absolutePath)) {
			File targetPathTemplateFile = pageEntity.entityFile(absolutePath);String params = "";
			return new PageService(runtimeContext, resource, targetPathTemplateFile, routeDir, params);
		} else {
			String extensionName = PathUtil.getExtensionName(absolutePath);
			String pathCells[] = clearHeadTailSlash(absolutePath).split("/");
			return createPageService(pathCells, extensionName);
		}
	}

	private String clearHeadTailSlash(String path) {
		return path.replaceAll("^/", "").replaceAll("/$", "");
	}

	private PageService createPageService(String[] pathCells, String extensionName) {
		
		int endOfExistDirIndex = findEndOfExistDirIndex(pathCells, extensionName);
		String path = getTemplateDirPath(pathCells, endOfExistDirIndex);
		File templatePath = getTemplatePath(path, extensionName);
		if (templatePath != null) {
			String params = selectParamsFromPath(pathCells, endOfExistDirIndex + 1);
			return new PageService(runtimeContext, resource, templatePath, routeDir, params);
		}
		return null;
	}

	private String getTemplateDirPath(String[] pathCells, int endOfExistDirIndex) {
		String path = "";
		for(int i=0; i < endOfExistDirIndex + 1; ++i) {
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
			String pathCell = pathCells[i] + "." + extensionName;
			path = new File(path, pathCell);
			if(pageEntity.entityExist(path.getPath())) {
				break;
			}
			endOfExistDirIndex = i;
		}
		return endOfExistDirIndex;
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

	private boolean isTemplateFile(File file) {
		return file.exists() || file.isFile();
	}

	private File getTemplatePath(String dirPath, String extensionName) {
		File file = new File(routeDir.getPath(), dirPath + "." + extensionName);
		if (isTemplateFile(file)) {
			return file;
		}
		extensionName = normalizeIndexFileExtensionName(extensionName);
		file = new File(routeDir.getPath(), dirPath + "/index." + extensionName);
		if (isTemplateFile(file)) {
			return file;
		}
		return null;
	}

	private String normalizeIndexFileExtensionName(String extensionName) {
		extensionName = extensionName.trim();
		for (String posibleName : IndexFilePosibleExtensionNames) {
			if (posibleName.equals(extensionName)) {
				return extensionName;
			}
		}
		return "html";
	}
}
