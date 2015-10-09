package com.taozeyu.calico;

import java.io.File;

import com.taozeyu.calico.copier.ResourceFileCopier;
import com.taozeyu.calico.copier.TargetDirectoryCleaner;
import com.taozeyu.calico.generator.Router;
import com.taozeyu.calico.resource.ResourceManager;
import com.taozeyu.calico.util.PathUtil;

public class Main {

	public static void main(String[] args) throws Exception {

		File targetPath = GlobalConfig.instance().getFile("target", "./");
		String rootMapToPath = GlobalConfig.instance().getString("root", "/index.html");
		
		File templatePath = GlobalConfig.instance().getFile("template", "./template");
		File resourcePath = GlobalConfig.instance().getFile("resource", "./resource");
		
		ResourceManager resource = new ResourceManager(resourcePath);
		Router router = new Router(resource, templatePath, rootMapToPath);

		new TargetDirectoryCleaner(targetPath).clean();
		new ResourceFileCopier(templatePath, targetPath).copy();
		new ContentBuilder(router, targetPath).buildFromRootFile();
	}

	private static String getElementFromArgs(String[] args, int index, String defaultValue) {
		if (index >= args.length) {
			return defaultValue;
		} else {
			return args[index];
		}
	}

	private static File getDirFromPath(String path) {
		File file;
		if(PathUtil.isAbsolutePath(path)) {
			file = new File(path);
		} else {
			path = path.replaceAll("^\\./", "");
			file = new File(System.getProperty("user.dir"), path);
		}
		if(!file.exists() || !file.isDirectory()) {
			throw new RuntimeException("directory not found " + file.getPath() +".");
		}
		return file;
	}

	private static String normalizePath(String path) {
		return path.replaceAll("\\\\", "/").replaceAll("(\\.(\\w|\\-)+/?)?$", "");
	}
}
