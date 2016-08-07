package com.taozeyu.calico.copier;

import com.taozeyu.calico.EntityPathContext;
import com.taozeyu.calico.RuntimeContext;

import java.io.*;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by taozeyu on 15/10/6.
 */
public class ResourceFileCopier {

    private final RuntimeContext runtimeContext;
    private final Pattern ignorePattern;

    public ResourceFileCopier(RuntimeContext runtimeContext) {
        this.runtimeContext = runtimeContext;
        this.ignorePattern = runtimeContext.getIgnoreCopy();
    }

    public void copy() throws IOException {
        System.out.println("");
        System.out.println("Copy resource files");

        copyAllFile(new File(
                runtimeContext.getTemplateDirectory(),
                EntityPathContext.EntityType.Asset.getDirectoryName()));
        for (String resourceAssetsPath : runtimeContext.getResourceAssetsPath()) {
            copyAllFile(runtimeContext.getResourceDirectory(), resourceAssetsPath);
        }
    }

    private void copyAllFile(File searchRootDir) throws IOException {
        copyAllFile(searchRootDir, "");
    }

    private void copyAllFile(File searchRootDir, String relativePath) throws IOException {
        File searchFile = new File(searchRootDir, relativePath);
        if (searchFile.exists()) {
            if (searchFile.isDirectory()) {
                for (String childFileName : searchFile.list()) {
                    copyAllFile(searchRootDir, relativePath +"/"+ childFileName);
                }
            } else {
                if (fileSouldBeCopied(searchFile)) {
                    System.out.println("copy file " + relativePath);
                    copyFile(searchFile, new File(runtimeContext.getTargetDirectory(), relativePath));
                }
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
