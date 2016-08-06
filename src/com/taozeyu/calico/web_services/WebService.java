package com.taozeyu.calico.web_services;

import com.sun.javafx.scene.shape.PathUtils;
import com.taozeyu.calico.RuntimeContext;
import com.taozeyu.calico.generator.PageService;
import com.taozeyu.calico.generator.Router;
import com.taozeyu.calico.util.PathUtil;
import com.taozeyu.calico.util.StringGeneratorPrinter;
import fi.iki.elonen.NanoHTTPD;

import javax.script.ScriptException;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

import static fi.iki.elonen.NanoHTTPD.Response.Status.*;

/**
 * Created by taozeyu on 15/10/9.
 */
public class WebService extends NanoHTTPD {

    private static final Map<String, String> ContentTypeMap = new HashMap<>();
    private static final String UnknownType = "application/octet-stream";
    private static final String[] NotResourceExtensionNames = new String[] {
            "html", "htm", "",
    };

    private static String[][] getContentTypeData() {
        // TODO We should add all content-type, See this page http://tool.oschina.net/commons
        return new String[][] {

                // basic resource
                {"css", "text/css"},
                {"xml", "text/xml"},

                // art resource
                {"jpe", "image/jpeg"},
                {"jpeg", "image/jpeg"},
                {"jpg", "application/x-jpg"},
                {"gif", "image/gif"},
                {"png", "application/x-png"},
        };
    }

    static {
        String[][] contentTypeData = getContentTypeData();
        for (String[] data : contentTypeData) {
            ContentTypeMap.put(data[0], data[1]);
        }
    }

    private final Router router;
    private final RuntimeContext runtimeContext;

    public WebService(RuntimeContext runtimeContext, Router router) {
        super(runtimeContext.getPort());
        this.runtimeContext = runtimeContext;
        this.router = router;
    }

    @Override
    public Response serve(IHTTPSession session) {
        try {
            return handleRequest(session);

        } catch (Exception e) {
            e.printStackTrace();
            return getErrorMessageResponse(INTERNAL_ERROR, "500 Internal Error:"+ e);
        }
    }

    private Response handleRequest(IHTTPSession session) throws IOException, ScriptException {
        String path = session.getUri().toString();
        if (isResourcePath(path)) {
            System.out.println("Get Request for Resource: " + path);
            return generateResourceResponse(path);
        } else {
            System.out.println("Get Request for Page: "+ path);
            return generatePageResponse(path);
        }
    }

    private boolean isResourcePath(String path) {
        String extensionName = PathUtil.getExtensionName(path, "");
        for (String notResourceExtensionName : NotResourceExtensionNames) {
            if (notResourceExtensionName.equals(extensionName)) {
                return false;
            }
        }
        return true;
    }

    private synchronized Response generatePageResponse(String path) throws IOException, ScriptException {
        PageService pageService = router.getPageService(path);
        if (pageService == null) {
            return getErrorMessageResponse(NOT_FOUND, "404 Page Not Found:"+ path);
        }
        StringGeneratorPrinter printer = new StringGeneratorPrinter();
        pageService.requestPage(printer);
        return new Response(OK, "text/html", printer.toString());
    }

    private Response generateResourceResponse(String path) throws FileNotFoundException {
        InputStream inputStream = null;

        if (router.existAssetAsEntity(path)) {
            inputStream = router.getAsset(path);

        } else if (isPathAllowedInTarget(path)) {
            File targetDirectory = runtimeContext.getTargetDirectory();
            File assetFile = new File(targetDirectory, path);
            if (assetFile.exists() && assetFile.isFile()) {
                inputStream = new FileInputStream(assetFile);
            }
        }
        if (inputStream != null) {
            String contentType = getContentTypeByExtensionName(PathUtil.getExtensionName(path));
            inputStream = new BufferedInputStream(inputStream, 1024);
            return new Response(OK, contentType, inputStream);
        }
        return getErrorMessageResponse(NOT_FOUND, "404 Resource Not Found:"+ path);
    }

    private boolean isPathAllowedInTarget(String path) {
        String[] pathComponents = PathUtil.splitComponents(path);
        for (String linkedPath : runtimeContext.getResourceAssetsPath()) {
            String[] linkedComponents = PathUtil.splitComponents(linkedPath);
            if (linkedComponents.length <= pathComponents.length) {
                boolean match = true;
                for (int i = 0; i< linkedComponents.length; i ++) {
                    if (!pathComponents[i].equals(linkedComponents[i])) {
                        match = false;
                        break;
                    }
                }
                if (match) {
                    return true;
                }
            }
        }
        return false;
    }

    private Response getErrorMessageResponse(Response.IStatus state, String errorMessage) {
        String contentType = "text/html";
        System.err.println(errorMessage);
        return new Response(state, contentType, errorMessage);
    }

    private String getContentTypeByExtensionName(String extensionName) {
        String contentType = ContentTypeMap.get(extensionName.trim().toLowerCase());
        if (contentType == null) {
            contentType = UnknownType;
        }
        return contentType;
    }
}
