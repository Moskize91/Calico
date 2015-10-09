package com.taozeyu.calico.util;

import com.taozeyu.calico.generator.Printer;

/**
 * Created by taozeyu on 15/10/9.
 */
public class StringGeneratorPrinter implements Printer {

    private final StringBuilder stringBuilder = new StringBuilder();

    @Override
    public void println(String content) {
        print(content);
        print("\n");
    }

    @Override
    public void print(String content) {
        stringBuilder.append(content);
    }

    @Override
    public String toString() {
        return stringBuilder.toString();
    }
}
