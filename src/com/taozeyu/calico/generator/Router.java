package com.taozeyu.calico.generator;

import java.io.File;
import java.io.InputStream;

import com.taozeyu.calico.EntityPathContext;
import com.taozeyu.calico.RuntimeContext;
import com.taozeyu.calico.resource.ResourceManager;
import com.taozeyu.calico.util.PathUtil;

public class Router {

	public static final String RootPath = "/";
	private static final String[] IndexFilePossibleExtensionNames = new String[]{
			"html", "htm",
	};

	private final RuntimeContext runtimeContext;
	private final EntityPathContext assetEntity;
	private final EntityPathContext pageEntity;
	private final ResourceManager resource;
	
	public Router(RuntimeContext runtimeContext, ResourceManager resource) {
		this.runtimeContext = runtimeContext;
		this.resource = resource;
		this.assetEntity = new EntityPathContext(
				runtimeContext,
				EntityPathContext.EntityType.Asset,
				EntityPathContext.EntityModule.Template, "/"
		);
		this.pageEntity = new EntityPathContext(
				runtimeContext,
				EntityPathContext.EntityType.Page,
				EntityPathContext.EntityModule.Template, "/"
		);
	}

	public boolean existAssetAsEntity(String relativePath) {
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
			String params = "";
			return new PageService(runtimeContext, resource, absolutePath, params);
		} else {
			String extensionName = PathUtil.getExtensionName(absolutePath);
			String noExtensionNamePath = PathUtil.clearExtensionName(clearHeadTailSlash(absolutePath));
			String pathCells[] = noExtensionNamePath.split("/");
			return createPageService(pathCells, extensionName);
		}
	}

	private String clearHeadTailSlash(String path) {
		return path.replaceAll("^/", "").replaceAll("/$", "");
	}

	private PageService createPageService(String[] pathCells, String extensionName) {
		int endOfExistDirIndex = findEndOfExistDirIndex(pathCells, extensionName);
		if (endOfExistDirIndex != -1) {
			String path = getTemplateDirPath(pathCells, endOfExistDirIndex);
			String validPagePath = findValidPagePath(path, extensionName);
			if (validPagePath != null) {
				String params = selectParamsFromPath(pathCells, endOfExistDirIndex + 1);
				return new PageService(runtimeContext, resource, validPagePath, params);
			}
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
		String basicPath = "";
		for(int i = 0; i < pathCells.length; ++ i) {
			basicPath += "/"+ pathCells[i];
			String pathCell = basicPath + "." + extensionName;
			if(pageEntity.entityExist(pathCell)) {
				endOfExistDirIndex = i;
			}
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

	private String findValidPagePath(String dirPath, String extensionName) {
		String path = dirPath + "." + extensionName;
		if (pageEntity.entityExist(path)) {
			return path;
		}
		extensionName = normalizeIndexFileExtensionName(extensionName);
		path = dirPath + "/index." + extensionName;
		if (pageEntity.entityExist(path)) {
			return path;
		}
		return null;
	}

	private String normalizeIndexFileExtensionName(String extensionName) {
		extensionName = extensionName.trim();
		for (String possibleName : IndexFilePossibleExtensionNames) {
			if (possibleName.equals(extensionName)) {
				return extensionName;
			}
		}
		return "html";
	}
}
