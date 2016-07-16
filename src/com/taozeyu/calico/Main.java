package com.taozeyu.calico;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
        RuntimeContext runtimeContext = configureRuntimeContext();

		ResourceManager resource = new ResourceManager(runtimeContext.getResourceDirecotry());
		Router router = new Router(runtimeContext, resource);

		String command = argumentsHandler.getCommand();

		if (command.toLowerCase().equals("build")) {
//			build(router, targetPath, templatePath);

		} else if (command.toLowerCase().equals("service")) {
			service(router);

		} else {
			throw new RuntimeException("Unknown command "+ command);
		}
	}

    private static RuntimeContext configureRuntimeContext() throws ScriptException {
        RuntimeContext runtimeContext = new RuntimeContext();
        runtimeContext.setSystemEntityDirectory(new File(getHomePath(), "lang"));

        File calicoDirectory = new File(System.getProperty("user.dir"));
        EntityPathContext entityPathContext = new EntityPathContext(
                runtimeContext,
                EntityPathContext.EntityType.JavaScript,
                EntityPathContext.EntityModule.Library,
                calicoDirectory, "/");
        ScriptContext initScriptContext = new ScriptContext(
                entityPathContext,
                runtimeContext);
        Object calicoInitialization = initScriptContext.require("/lang/calico_initialization");
        initScriptContext.engine().put("__calico_initialization", calicoInitialization);

        String head = "var __calico_configuration = new __calico_initialization.Configuration();" +
                      "(function(configure) {" +
                      "var __calico_configuration = undefined;\n"; //mask variables
        String footer = "}) (__calico_configuration.configure);\n";

        if (entityPathContext.entityExist(".calico")) {
            InputStream configurationInputStream = entityPathContext.inputStreamOfFile(".calico");
            initScriptContext.loadScriptFile(configurationInputStream, head, footer);
        } else {
            initScriptContext.engine().eval(head + footer);
        }
        String templateDirectory = (String) initScriptContext.engine()
                .eval("__calico_configuration.value_of_string('template_directory')");
        String targetDirectory = (String) initScriptContext.engine()
                .eval("__calico_configuration.value_of_string('target_directory')");
        String resourceDirectory = (String) initScriptContext.engine()
                .eval("__calico_configuration.value_of_string('resource_directory')");
        String rootPage = (String) initScriptContext.engine()
                .eval("__calico_configuration.value_of_string('root_page')");

        runtimeContext.setTemplateDirectory(getDirectoryWithPath(templateDirectory));
        runtimeContext.setTargetDirectory(getDirectoryWithPath(targetDirectory));
        runtimeContext.setResourceDirecotry(getDirectoryWithPath(resourceDirectory));
        runtimeContext.setRootPage(rootPage);

        return runtimeContext;
    }

    private static File getDirectoryWithPath(String path) {
        if (PathUtil.isAbsolutePath(path)) {
            return new File(path);
        } else {
            return new File(System.getProperty("user.dir"), path);
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
