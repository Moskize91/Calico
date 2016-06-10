package com.taozeyu.calico;

import com.taozeyu.calico.util.GitHelper;
import com.taozeyu.calico.util.PathUtil;

import java.io.*;
import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by taozeyu on 15/10/9.
 */
public class GlobalConfig {

    private static Charset defaultCharset = Charset.forName("UTF-8");

    private static class SingleGlobalConfig {
        private static GlobalConfig config = new GlobalConfig();
    }

    public static GlobalConfig instance() {
        return SingleGlobalConfig.config;
    }

    private final File configFile;
    private final Map<String, String> configMap;

    private GlobalConfig() {
        try {
            configFile = getConfigFile();
            try (BufferedReader reader = getFileReader(configFile)) {
                configMap = getConfigMapFromReader(reader);
            }
        } catch (IOException e) {
            throw new RuntimeException(e.toString());
        }
    }

    private File getConfigFile() throws FileNotFoundException {
        File file = new File(System.getProperty("user.dir"), ".calico");
        if (!file.exists() || !file.isFile()) {
            throw new FileNotFoundException("File not found : "+ file.getPath());
        }
        return file;
    }

    private BufferedReader getFileReader(File file) throws FileNotFoundException, UnsupportedEncodingException {
        int bufferLength = 1024;
        return new BufferedReader(new InputStreamReader(
                new FileInputStream(file), defaultCharset), bufferLength);
    }

    private Map<String, String> getConfigMapFromReader(BufferedReader reader) throws IOException {
        Pattern namePattern = Pattern.compile("^(\\w|-|_)+");
        Map<String, String> configMap = new HashMap<>();
        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
            line = line.trim();
            if (!line.equals("")) {
                Matcher matcher = namePattern.matcher(line);
                matcher.find();
                String name = matcher.group();
                matcher = namePattern.matcher(line);
                String value = matcher.replaceFirst("").trim();
                if (value.equals("")) {
                    value = "true";
                }
                configMap.put(name, value);
            }
        }
        return configMap;
    }

    public Charset getCharset() {
        return defaultCharset;
    }

    public File configFileDir() {
        return configFile.getParentFile();
    }

    public String getString(String name, String defaultValue) {
        String value = configMap.get(name);
        if (value == null) {
            value = defaultValue;
        }
        return value;
    }

    public String getString(String name) {
        return configMap.get(name);
    }

    public String[] getStringArray(String name) {
        return getStringArray(name, "");
    }

    public String[] getStringArray(String name, String defaultValue) {
        LinkedList<String> list = new LinkedList<>();
        for (String str : getString(name, defaultValue).split("\\s+")) {
            str = str.trim();
            if (!"".equals(str)) {
                list.add(str);
            }
        }
        return list.toArray(new String[list.size()]);
    }

    public Pattern getPattern(String name) {
        String neverMatchRegx = "$^";
        return getPattern(name, neverMatchRegx);
    }

    public Pattern getPattern(String name, String defaultValue) {
        String[] strArr = getStringArray(name, defaultValue);
        String str = String.join("|", strArr);
        if (str.length() >= 2) {
            return Pattern.compile("("+str+")");
        } else {
            return Pattern.compile(str);
        }
    }

    public File getFile(String name) {
        return getFile(name, "");
    }

    public File getFile(String name, String defaultValue) {
        String value = getString(name, defaultValue);
        if (GitHelper.instance.isGitURL(value)) {
            return GitHelper.instance.getLocalDirectoryOfGitURL(value);
        } else {
            return PathUtil.getFile(value, configFileDir().getPath());
        }
    }

    public int getInt(String name) {
        return getInt(name, 0);
    }

    public int getInt(String name, int defaultValue) {
        String value = getString(name);
        if (value != null) {
            return Integer.valueOf(value);
        } else {
            return defaultValue;
        }
    }
}
