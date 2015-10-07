package com.taozeyu.calico;

/**
 * Created by taozeyu on 15/10/7.
 */
public class SystemJavaScriptLog {

    public void i(Object content) {
        System.out.println(content);
    }

    public void e(Object content) {
        System.err.println(content);
    }
}
