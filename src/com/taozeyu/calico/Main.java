package com.taozeyu.calico;

import java.io.File;

import com.taozeyu.calico.generator.Router;
import com.taozeyu.calico.resource.ResourceManager;

public class Main {

	public static void main(String[] args) throws Exception {

		File projectPath = getDirFromPath(getElementFromArgs(args, 0, "./"));
		File targetPath = getDirFromPath(getElementFromArgs(args, 1, "./"));
		String rootMapToPath = getElementFromArgs(args, 2, "/index.html");
		
		File rootPath = new File(projectPath.getPath(), "template");
		File resourcePath = new File(projectPath.getPath(), "resource");
		
		ResourceManager resource = new ResourceManager(resourcePath);
		Router router = new Router(resource, rootPath, rootMapToPath);
		
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
		if(isAbsolutePath(path)) {
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

	private static boolean isAbsolutePath(String path) {
		return path.matches("([a-zA-Z]+:)?(\\\\|/).*");
	}
}
