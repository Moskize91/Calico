package com.taozeyu.calico;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import com.taozeyu.calico.copier.ResourceFileCopier;
import com.taozeyu.calico.copier.TargetDirectoryCleaner;
import com.taozeyu.calico.generator.Router;
import com.taozeyu.calico.resource.ResourceManager;
import com.taozeyu.calico.script.ScriptContext;
import com.taozeyu.calico.util.PathUtil;
import com.taozeyu.calico.web_services.WebService;

import javax.script.ScriptException;

public class Main {

    private static final String CurrentVersion = "1.0.0";

	public static void main(String[] args) throws Exception {
        ArgumentsHandler argumentsHandler = new ArgumentsHandler(args) {{
            abbreviation("v", "version");
        }};
        if (argumentsHandler.getCommand() == null) {
            if (argumentsHandler.hasValue("version")) {
                System.out.println(CurrentVersion);
                System.exit(0);
            }
        }
        RuntimeContext runtimeContext = new RuntimeContext();
        runtimeContext.setSystemEntityDirectory(new File(getHomePath()));

        File moduleDirectory = runtimeContext.getSystemEntityDirectory();
        EntityPathContext entityPathContext = new EntityPathContext(
                runtimeContext,
                EntityPathContext.EntityType.JavaScript,
                EntityPathContext.EntityModule.SystemLibrary,
                new File(moduleDirectory, "lang"), "/");
        ScriptContext initScriptContext = new ScriptContext(
                entityPathContext,
                runtimeContext);
        System.out.println(initScriptContext.engine().eval("__script_context.require(1)(2, 5)"));


        if (true) {
            return;
        }


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

    private static String getHomePath() {
        String path = Main.class.getResource("Main.class").toString();
        path = PathUtil.toUnixLikeStylePath(path);
        int index = path.lastIndexOf("!/com/taozeyu/calico/Main.class");
        if (index > 0) {
            path = path.substring(0, index);
            String[] components = PathUtil.splitComponents(path);
            components = Arrays.copyOfRange(components, 0, components.length - 2);
            boolean isRoot = true;
            path = PathUtil.pathFromComponents(components, isRoot);
        } else {
            index = path.lastIndexOf("out/production/Calico/com/taozeyu/calico/Main.class");
            path = path.substring(0, index);
            path = path.replaceAll("^file:", "");
        }
        return path;
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
		Thread hookThread = new Thread(() -> {
            whenShutdown.run();
            synchronized (waitForHookLock) {
                waitForHookLock.notifyAll();
            }
        });
		Runtime.getRuntime().addShutdownHook(hookThread);
		synchronized (waitForHookLock) {
			waitForHookLock.wait();
		}
	}
}
