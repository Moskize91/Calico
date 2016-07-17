package com.taozeyu.calico.generator;

import com.taozeyu.calico.EntityPathContext;
import com.taozeyu.calico.RuntimeContext;
import com.taozeyu.calico.resource.ResourceManager;
import com.taozeyu.calico.script.ScriptContext;

import javax.script.ScriptException;
import java.io.*;
import java.util.HashMap;

/**
 * Created by taozeyu on 15/10/9.
 */
public class PageService {

    private final RuntimeContext runtimeContext;
    private final ResourceManager resource;

    private final String pageAbsolutionPath;
    private final String params;

    PageService(RuntimeContext runtimeContext, ResourceManager resource, String pageAbsolutionPath, String params) {
        this.runtimeContext = runtimeContext;
        this.resource = resource;
        this.pageAbsolutionPath = pageAbsolutionPath;
        this.params = params;
    }

    public void requestPage(Printer printStream) throws IOException, ScriptException {

        try (Reader reader = getTemplateReader()){
            ScriptContext scriptContext = createScriptContext();
            String pageContent = getFileContentFromReader(reader);
            scriptContext.loadViewScriptFile(pageContent, new HashMap<String, Object>() {{
                put("resource", resource);
                put("params", params);
            }});
        } catch (ScriptException e) {
            System.err.println("Error"+e.getMessage()+"(from "+ pageAbsolutionPath +")");
            throw e;
        }
    }

    private ScriptContext createScriptContext() throws IOException, ScriptException {
        EntityPathContext entityPathContext = new EntityPathContext(
                runtimeContext,
                EntityPathContext.EntityType.JavaScript,
                EntityPathContext.EntityModule.Template, "/"
        );
        return new ScriptContext(entityPathContext, runtimeContext);
    }

    private EntityPathContext createPageRelativeEntityPathContext() {
        EntityPathContext rootContext = new EntityPathContext(
                runtimeContext,
                EntityPathContext.EntityType.Page,
                EntityPathContext.EntityModule.Template, "/"
        );
        return rootContext.findContext(pageAbsolutionPath);
    }

    // Nashorn engine would omit some content if read from a long stream.
    // But all will be fine if buffered them before reading.
    // I don't know why.
    private String getFileContentFromReader(Reader reader) throws IOException {
        StringBuilder sb = new StringBuilder();
        for(int ch = reader.read(); ch >= 0; ch = reader.read()) {
            sb.append((char) ch);
        }
        return sb.toString();
    }

    private Reader getTemplateReader() throws IOException {
        RequireListReader templateReader = new RequireListReader(
                createPageRelativeEntityPathContext(),
                pageAbsolutionPath
        );
        RequireListReader layoutReader = templateReader.getLayoutRequireListReader();
        if (layoutReader != null) {
            layoutReader.setYieldReader(templateReader);
            return new HtmlTemplateReader(layoutReader);
        } else {
            return new HtmlTemplateReader(templateReader);
        }
    }
}
