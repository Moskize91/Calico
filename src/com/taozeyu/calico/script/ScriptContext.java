package com.taozeyu.calico.script;

import com.taozeyu.calico.EntityPathContext;
import com.taozeyu.calico.RuntimeContext;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.File;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.nio.charset.Charset;

/**
 * Created by taozeyu on 16/7/16.
 */
public class ScriptContext {

    private static final Charset LibraryCharset = Charset.forName("UTF-8");
    private final ScriptEngine engine;
    private final RuntimeContext runtimeContext;

    public ScriptContext(EntityPathContext entityPathContext,
                         RuntimeContext runtimeContext) throws ScriptException {
        this(new ScriptEngineManager().getEngineByName("nashorn"),
                entityPathContext, runtimeContext);
    }

    private ScriptContext(ScriptEngine engine,
                          EntityPathContext entityPathContext,
                          RuntimeContext runtimeContext) throws ScriptException {
        this.runtimeContext = runtimeContext;
        this.engine = engine;
        this.engine.getContext().setWriter(new OutputStreamWriter(System.out, LibraryCharset));
        this.engine.getContext().setErrorWriter(new OutputStreamWriter(System.err, LibraryCharset));
        this.engine.put("__script_context", this);
        this.engine.eval(readerOfRequireJS());
    }

    private Reader readerOfRequireJS() {
        File javascriptLang = new File(runtimeContext.getSystemEntityDirectory(), "lang");
        EntityPathContext entityPathContext = new EntityPathContext(
                runtimeContext,
                EntityPathContext.EntityType.JavaScript,
                EntityPathContext.EntityModule.SystemLibrary,
                javascriptLang, "/");
        return entityPathContext.readerOfFile("/require.js");
    }

    public ScriptEngine engine() {
        return engine;
    }

    public Object require(String path) throws ScriptException {
        ScriptEngine e = new ScriptEngineManager().getEngineByName("nashorn");
        return e.eval("function fuck(a, b) {return a + b;}");
    }
}
