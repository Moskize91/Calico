package com.taozeyu.calico;

import java.io.File;

import com.taozeyu.calico.copier.ResourceFileCopier;
import com.taozeyu.calico.copier.TargetDirectoryCleaner;
import com.taozeyu.calico.generator.Router;
import com.taozeyu.calico.resource.ResourceManager;

public class Main {

	public static void main(String[] args) throws Exception {

		File targetPath = GlobalConfig.instance().getFile("target", "./");
		File templatePath = GlobalConfig.instance().getFile("template", "./template");
		File resourcePath = GlobalConfig.instance().getFile("resource", "./resource");
		String rootMapToPath = GlobalConfig.instance().getString("root", "/index.html");
		
		ResourceManager resource = new ResourceManager(resourcePath);
		Router router = new Router(resource, templatePath, rootMapToPath);

		new TargetDirectoryCleaner(targetPath).clean();
		new ResourceFileCopier(templatePath, targetPath).copy();
		new ContentBuilder(router, targetPath).buildFromRootFile();
	}
}
