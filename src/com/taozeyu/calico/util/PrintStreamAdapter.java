package com.taozeyu.calico.util;

import com.taozeyu.calico.generator.Printer;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

/**
 * Created by taozeyu on 15/10/9.
 */
public class PrintStreamAdapter extends PrintStream implements Printer {

    public PrintStreamAdapter(OutputStream out, boolean autoFlush, String encoding) throws UnsupportedEncodingException {
        super(out, autoFlush, encoding);
    }
}
