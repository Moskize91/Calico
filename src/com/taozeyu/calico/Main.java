package com.taozeyu.calico;

import java.io.File;

import com.taozeyu.calico.generator.Router;
import com.taozeyu.calico.resource.ResourceManager;

public class Main {

	public static void main(String[] args) throws Exception {
		
		if(args.length != 3) {
			throw new RuntimeException("need 3 arguments, not " + args.length + ".");
		}
		File projectPath = getDirFromPath(args[0]);
		File targetPath = getDirFromPath(args[1]);
		String rootMapToPath = args[2];
		
		File rootPath = new File(projectPath.getPath(), "tempate");
		File resourcePath = new File(projectPath.getPath(), "resource");
		
		ResourceManager resource = new ResourceManager(resourcePath);
		Router router = new Router(resource, rootPath, rootMapToPath);
		
		new ContentBuilder(router, targetPath).buildFromRootFile();
	}
	
	private static File getDirFromPath(String path) {
		File file;
		if(isAbsolutePath(path)) {
			file = new File(path);
		} else {
			file = new File(System.getProperty("user.dir"), path);
		}
		if(!file.exists() || !file.isDirectory()) {
			throw new RuntimeException("directory not found " + file.getPath() +".");
		}
		return file;
	}

	private static boolean isAbsolutePath(String path) {
		return path.matches("([a-zA-Z]+:)?(\\|/)");
	}
}
