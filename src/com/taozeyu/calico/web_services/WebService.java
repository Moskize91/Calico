package com.taozeyu.calico.web_services;

import com.taozeyu.calico.GlobalConfig;
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

    private static String[][] getContentTypeData() {
        // TODO We should add all content-type, See this page http://tool.oschina.net/commons
        return new String[][] {
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

    private static final String[] NotResourceExtensionNames = new String[] {
        "html", "htm", "",
    };

    private final Router router;

    public WebService(Router router) {
        super(GlobalConfig.instance().getInt("port", 8080));
        this.router = router;
    }

    @Override
    public Response serve(IHTTPSession session) {
        try {
            synchronized (this) {
                return handleRequest(session);
            }
        } catch (Exception e) {
            return getErrorMessageResponse(INTERNAL_ERROR, "500 Internal Error:"+ e);
        }
    }

    private Response handleRequest(IHTTPSession session) throws IOException, ScriptException {
        String path = session.getUri().toString();
        if (isResourcePath(path)) {
            return generateResourceResponse(path);
        } else {
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

    private Response generatePageResponse(String path) throws IOException, ScriptException {
        PageService pageService = router.getPageService(path);
        if (pageService == null) {
            return getErrorMessageResponse(NOT_FOUND, "404 Page Not Found:"+ path);
        }
        StringGeneratorPrinter printer = new StringGeneratorPrinter();
        pageService.requestPage(printer);
        return new Response(OK, "text/html", printer.toString());
    }

    private Response generateResourceResponse(String path) throws FileNotFoundException {
        File resourceFile = router.getFile(path);
        if (isFile(resourceFile)) {
            return getErrorMessageResponse(NOT_FOUND, "404 Resource Not Found:"+ path);
        }
        String contentType = getContentTypeByExtensionName(PathUtil.getExtensionName(path));
        InputStream is = new BufferedInputStream(new FileInputStream(resourceFile), 1024);
        return new Response(OK, contentType, is);
    }

    private boolean isFile(File file) {
        return file.exists() && file.isFile();
    }

    private Response getErrorMessageResponse(Response.IStatus state, String errorMessage) {
        String contentType = "text/html";
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
