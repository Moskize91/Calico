package com.taozeyu.calico;

import java.io.File;

import com.taozeyu.calico.generator.Router;
import com.taozeyu.calico.resource.ResourceManager;

public class Main {

	public static void main(String[] args) throws Exception {
		File rootPath = new File(System.getProperty("user.dir"), "project/route");
		File resourcePath = new File(System.getProperty("user.dir"), "project/resource");
		File targetDir = new File(System.getProperty("user.dir"), "resource");
		
		ResourceManager resource = new ResourceManager(resourcePath);
		Router router = new Router(resource, rootPath, "/post.html");
		
		new ContentBuilder(router, targetDir).buildFromRootFile();
	}
}
