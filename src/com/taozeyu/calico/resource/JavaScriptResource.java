package com.taozeyu.calico.resource;

import com.taozeyu.calico.SystemJavaScriptLog;

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.io.*;
import java.nio.charset.Charset;

/**
 * Created by taozeyu on 15/10/7.
 */
public class JavaScriptResource {

    private static String[] SystemLibNames = new String[] {
        "test"
    };

    private static final int BufferedSize = 1024;

    private static final String LibraryPath = "javascript";
    private static final Charset LibraryCharset = Charset.forName("UTF-8");

    public void loadSystemJavaScriptLib(ScriptEngine engine) throws ScriptException, IOException {

        loadLogObject(engine);
        ClassLoader loader = getClass().getClassLoader();

        for (String libName : SystemLibNames) {
            String path = LibraryPath +"/"+ libName +".js";
            InputStream is = loader.getResourceAsStream(path);
            if (is == null) {
                File file = new File(System.getProperty("user.dir"), path);
                is = new FileInputStream(file);
            }
            loadJavaScript(is, engine);
        }
    }

    public void loadJavaScript(InputStream inputStream, ScriptEngine engine) throws ScriptException, IOException {
        Reader reader = getReaderFromFile(inputStream);
        try {
            engine.getContext().setWriter(new OutputStreamWriter(System.out, LibraryCharset));
            engine.getContext().setErrorWriter(new OutputStreamWriter(System.err, LibraryCharset));
            engine.eval(reader);
        } finally {
            reader.close();
        }
    }

    private boolean isExtensionNameJs(File file) {
        return file.getName().matches(".+\\.(?i)js$");
    }

    private Reader getReaderFromFile(InputStream inputStream) {
        return  new InputStreamReader(new BufferedInputStream(inputStream, BufferedSize), LibraryCharset);
    }

    private void loadLogObject(ScriptEngine engine) {
        engine.put("Log", new SystemJavaScriptLog());
    }
}
