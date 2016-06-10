package com.taozeyu.calico.util;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by taozeyu on 16/6/10.
 */
public class GitHelper {

    public static final GitHelper instance = new GitHelper();
    private static final Pattern gitURLPattern = Pattern.compile("^git@[\\w\\-_]+(\\.[\\w\\-_]+)*:[\\w\\-_]+(/[\\w\\-_]+)(\\.[\\w\\-_]+)*\\.git$");
    private static final Pattern rearOfURL = Pattern.compile("(/[\\w\\-_]+)(\\.[\\w\\-_]+)*\\.git$");

    private GitHelper() {}

    public boolean isGitURL(String url) {
        return gitURLPattern.matcher(url).find();
    }

    public String getRepositoryName(String url) {
        String repositoryName = null;
        if (isGitURL(url)) {
            Matcher matcher = rearOfURL.matcher(url);
            if (matcher.find()) {
                repositoryName = matcher.group().replaceAll("(^/|\\.git$)", "");
            }
        }
        return repositoryName;
    }

    public File getLocalDirectoryOfGitURL(String url) {
        File localDir = null;
        String repositoryName = getRepositoryName(url);
        if (repositoryName != null) {
            localDir = new File(System.getProperty("user.dir"), repositoryName);
            if (!localDir.exists()) {
                int cloneExitCode;
                try {
                    Process process = Runtime.getRuntime().exec(new String[]{"git", "clone", url, repositoryName});
                    catchInputStream(process.getInputStream(), System.out);
                    catchInputStream(process.getErrorStream(), System.err);
                    cloneExitCode = process.waitFor();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                if (cloneExitCode != 0) {
                    throw new RuntimeException("git clone exit with code "+ cloneExitCode);
                }
            }
        }
        return localDir;
    }

    private void catchInputStream(InputStream is, PrintStream out) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        new Thread(() -> {
            try {
                for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                    out.println(line);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }
}
