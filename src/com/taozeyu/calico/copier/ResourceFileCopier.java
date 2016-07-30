package com.taozeyu.calico.copier;

import com.taozeyu.calico.RuntimeContext;

import java.io.*;
import java.util.regex.Pattern;

/**
 * Created by taozeyu on 15/10/6.
 */
public class ResourceFileCopier {

    private final Pattern ignorePattern;

    private File templateDir;
    private File targetDir;

    public ResourceFileCopier(RuntimeContext runtimeContext, File templateDir, File targetDir) {
        this.ignorePattern = runtimeContext.getIgnoreCopy();
        this.templateDir = templateDir;
        this.targetDir = targetDir;
    }

    public void copy() throws IOException {
        System.out.println("");
        System.out.println("Copy resource files");
        copyAllFile("");
    }

    private void copyAllFile(String relativePath) throws IOException {
        File templateFile = new File(templateDir, relativePath);
        if (templateFile.isDirectory()) {
            for (String childFileName : templateFile.list()) {
                copyAllFile(relativePath +"/"+ childFileName);
            }
        } else {
            if (fileSouldBeCopied(templateFile)) {
                System.out.println("copy file " + relativePath);
                copyFile(templateFile, new File(targetDir, relativePath));
            }
        }
    }

    private boolean fileSouldBeCopied(File file) {
        // extension name is not html or htm.
        return !isReservedFile(file) &&
               !file.getName().matches(".*\\.html?$") &&
               !ignorePattern.matcher(file.getPath()).find();
    }

    private boolean isReservedFile(File file) {
        return file.getName().startsWith(".");
    }

    private void copyFile(File sourceFile, File targetFile) throws IOException {

        targetFile.getParentFile().mkdirs();

        int bufferLength = 1024;
        InputStream is = new BufferedInputStream(new FileInputStream(sourceFile), bufferLength);
        OutputStream os = new BufferedOutputStream(new FileOutputStream(targetFile), bufferLength);

        try {
            byte[] buffer = new byte[bufferLength];

            for (int len = is.read(buffer); len >= 0; len = is.read(buffer)) {
                os.write(buffer, 0, len);
            }
        } finally {
            is.close();
            os.flush();
            os.close();
        }
    }
}
