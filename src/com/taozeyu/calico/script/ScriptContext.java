package com.taozeyu.calico.script;

import com.taozeyu.calico.EntityPathContext;
import com.taozeyu.calico.RuntimeContext;
import com.taozeyu.calico.util.PathUtil;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.*;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by taozeyu on 16/7/16.
 */
public class ScriptContext {

    private static final Charset LibraryCharset = Charset.forName("UTF-8");
    private final ScriptContext rootScriptContext;
    private final ScriptEngine engine;
    private final EntityPathContext entityPathContext;
    private final RuntimeContext runtimeContext;
    private final Map<EntityPathContext.ContextResult, Object> requiredCache;

    public ScriptContext(EntityPathContext entityPathContext,
                         RuntimeContext runtimeContext) throws ScriptException {
        this(null, entityPathContext, runtimeContext);
    }

    private ScriptContext(ScriptContext rootScriptContext,
                          EntityPathContext entityPathContext,
                          RuntimeContext runtimeContext) throws ScriptException {
        if (rootScriptContext != null) {
            this.rootScriptContext = rootScriptContext;
            this.requiredCache = null;
        } else {
            this.rootScriptContext = this;
            this.requiredCache = new HashMap<>();
        }
        this.engine = new ScriptEngineManager().getEngineByName("nashorn");
        this.entityPathContext = entityPathContext;
        this.runtimeContext = runtimeContext;
        this.engine.getContext().setWriter(new OutputStreamWriter(System.out, LibraryCharset));
        this.engine.getContext().setErrorWriter(new OutputStreamWriter(System.err, LibraryCharset));
        this.engine.put("__script_context", this);
        this.engine.eval("function __require(path) { return __script_context.require(path); };");
    }

    public ScriptEngine engine() {
        return engine;
    }

    public Object require(String path) throws ScriptException {
        String originalPath = path;
        if (!entityPathContext.entityExist(path + ".js")) {
            String components[] = PathUtil.splitComponents(path);
            String newComponents[] = new String[components.length + 1];
            for (int i = 0; i < components.length; i++) {
                newComponents[i] = components[i];
            }
            newComponents[components.length] = "index";
            boolean isRoot = true;
            path = PathUtil.pathFromComponents(newComponents, isRoot);
            if (!entityPathContext.entityExist(path + ".js")) {
                throw new ScriptException("require not found `" + originalPath + "`");
            }
        }

        EntityPathContext.ContextResult result = entityPathContext.findFileAndParentContext(path);
        Object requiredObject = rootScriptContext.requiredCache.get(result);

        if (requiredObject == null) {
            ScriptContext subcontext = new ScriptContext(rootScriptContext,
                    result.getContext(),
                    runtimeContext);
            InputStream inputStream = entityPathContext.inputStreamOfFile(path + ".js");
            requiredObject = subcontext.loadScriptFile(inputStream);
            rootScriptContext.requiredCache.put(result, requiredObject);
        }
        return requiredObject;
    }

    public Object loadScriptFile(InputStream inputStream) throws ScriptException {
        String head = "var __require_module = {};\n" +
                      "(function(require, M) {" +
                      "var __require_module = undefined;\n";
        String footer = "\n}) (__require, __require_module);\n" +
                        "__require_module;";
        return loadScriptFile(inputStream, head, footer);
    }

    public Object loadViewScriptFile(String viewContent, Map<String, Object> params) throws ScriptException {
        String head = "(function(require";
        String footer = "\n}) (__require";
        for (String paramName : params.keySet()) {
            Object paramValue = params.get(paramName);
            engine.put("__"+ paramName, paramValue);
            head += ", "+ paramName;
            footer += ", __"+ paramName;
        }
        head += ") {";
        footer += ");";
        for (String paramName : params.keySet()) {
            head += "var __" + paramName + " = undefined;\n";
        }
        head += "var __require = undefined;" + // mask for user's code.
                "var __require_module = undefined;" + // mask for user's code.
                "var __script_context = undefined;\n"; // mask for user's code.
        return engine.eval(head + viewContent + footer);
    }

    public Object loadScriptFile(InputStream inputStream, String head, String footer) throws ScriptException {
        Reader reader = new InputStreamReader(inputStream);
        return loadScriptFile(reader, head, footer);
    }

    public Object loadScriptFile(Reader reader, String head, String footer) throws ScriptException {
        head += "var __require = undefined;" + // mask for user's code.
                "var __require_module = undefined;" + // mask for user's code.
                "var __script_context = undefined;\n"; // mask for user's code.
        reader = new WrapReader(reader, head, footer);
        return engine.eval(reader);
    }

    private static class WrapReader extends Reader {

        private final Reader reader;
        private final String head, footer;
        private int index;
        private int fileLen;

        private WrapReader(Reader reader, String head, String footer) {
            this.reader = reader;
            this.head = head;
            this.footer = footer;
            this.index = 0;
        }

        @Override
        public int read(char[] cbuf, int off, int len) throws IOException {
            int readCount = 0;
            if (index < head.length()) {
                int lenNeedRead = Math.min(head.length() - index, len);
                for (int i = 0; i < lenNeedRead; i++) {
                    cbuf[off + i] = head.charAt(index + i);
                }
                index += lenNeedRead;
                off += lenNeedRead;
                len -= lenNeedRead;
                readCount += lenNeedRead;
            }
            if (len == 0) {
                return readCount;
            }
            int readFromReader = reader.read(cbuf, off, len);

            if (readFromReader != -1) {
                index += readFromReader;
                off += readFromReader;
                len -= readFromReader;
                readCount += readFromReader;
                fileLen += readFromReader;
            }
            if (len == 0) {
                return readCount;
            }
            int footerStartIndex = index - fileLen - head.length();
            if (footerStartIndex >= footer.length()) {
                return -1; // touch the terminal.
            }
            int lenNeedRead = Math.min(footer.length() - footerStartIndex, len);
            for (int i = 0; i < lenNeedRead; i++) {
                cbuf[off + i] = footer.charAt(footerStartIndex + i);
            }
            index += lenNeedRead;
            readCount += lenNeedRead;

            return readCount;
        }

        @Override
        public void close() throws IOException {
            reader.close();
        }
    }
}
