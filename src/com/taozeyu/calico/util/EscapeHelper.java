package com.taozeyu.calico.util;

import java.util.HashMap;

/**
 * Created by taozeyu on 15/10/22.
 */
public class EscapeHelper {

    // There are more escape should inserted.
    // Please check the page: http://tool.oschina.net/commons?type=2
    private static final HashMap<Character, String> EscaperMap = new HashMap<Character, String>() {{
        put('"', "&quot;");
        put('&', "&amp;");
        put('<', "&lt;");
        put('>', "&gt;");
        put(' ', "&nbsp;");
    }};

    public static String escape(char c) {
        String str = EscaperMap.get(c);
        if (str == null) {
            return String.valueOf(c);
        }
        return str;
    }

    public static String escape(String str) {
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<str.length(); ++i) {
            char c = str.charAt(i);
            String escapeStr = EscaperMap.get(c);
            if (escapeStr == null) {
                sb.append(c);
            } else {
                sb.append(escapeStr);
            }
        }
        return sb.toString();
    }
}
