package com.taozeyu.calico;

import java.io.File;
import java.io.IOException;

import com.taozeyu.calico.copier.ResourceFileCopier;
import com.taozeyu.calico.copier.TargetDirectoryCleaner;
import com.taozeyu.calico.generator.Router;
import com.taozeyu.calico.resource.ResourceManager;
import com.taozeyu.calico.web_services.WebService;

import javax.script.ScriptException;

public class Main {

	public static void main(String[] args) throws Exception {

		checkArgs(args);

		File targetPath = GlobalConfig.instance().getFile("target", "./");
		File templatePath = GlobalConfig.instance().getFile("template", "./template");
		File resourcePath = GlobalConfig.instance().getFile("resource", "./resource");
		String rootMapToPath = GlobalConfig.instance().getString("root", "/index.html");
		
		ResourceManager resource = new ResourceManager(resourcePath);
		Router router = new Router(resource, templatePath, rootMapToPath);

		String command = args[0];

		if (command.toLowerCase().equals("build")) {
			build(router, targetPath, templatePath);

		} else if (command.toLowerCase().equals("service")) {
			service(router);

		} else {
			throw new RuntimeException("Unknown command "+ command);
		}
	}

	private static void build(Router router, File targetPath, File templatePath)
			throws IOException, ScriptException {

		new TargetDirectoryCleaner(targetPath).clean();
		new ResourceFileCopier(templatePath, targetPath).copy();
		new ContentBuilder(router, targetPath).buildFromRootFile();
	}

	private static void service(Router router) throws IOException, InterruptedException {
		final WebService webService = new WebService(router);
		webService.start();
		System.out.println( "\nRunning! Point your browser to http://127.0.0.1:"+ webService.getListeningPort() +"/ \n" );
		waitForCtrlCHook(() -> {
            webService.closeAllConnections();
            System.out.println("Press Ctrl+C to shutdown service.");
        });
	}

	private static void checkArgs(String[] args) {
		if (args.length != 1) {
			throw new RuntimeException("Need just only ONE argument!");
		}
	}

	private static void waitForCtrlCHook(Runnable whenShutdown) throws InterruptedException {
		final Object waitForHookLock = new Object();
		Thread hookThread = new Thread(new Runnable() {
			@Override
			public void run() {
				whenShutdown.run();
				synchronized (waitForHookLock) {
					waitForHookLock.notifyAll();
				}
			}
		});
		Runtime.getRuntime().addShutdownHook(hookThread);
		synchronized (waitForHookLock) {
			waitForHookLock.wait();
		}
	}
}
