package com.taozeyu.calico;

import java.io.File;

import com.taozeyu.calico.generator.Router;

public class Main {

	public static void main(String[] args) throws Exception {
		File rootPath = new File(System.getProperty("user.dir"), "project/route");
		File targetDir = new File(System.getProperty("user.dir"), "resource");
		Router router = new Router(rootPath, "/post.html");
		
		new ContentBuilder(router, targetDir).buildFromRootFile();
	}
}
