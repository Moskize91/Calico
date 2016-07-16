package com.taozeyu.calico;

import com.taozeyu.calico.exception.EntityPathContextException;
import com.taozeyu.calico.util.PathUtil;

import java.io.*;
import java.util.*;

/**
 * Created by taozeyu on 16/7/3.
 */
public class EntityPathContext {

    private final RuntimeContext runtimeContext;
    private final EntityType entityType;
    private final EntityModule entityModule;
    private final File moduleDirectory;
    private final String absolutionPath;

    public EntityPathContext(RuntimeContext runtimeContext,
                             EntityType entityType, EntityModule entityModule,
                             File moduleDirectory, String absolutionPath) {
        this.runtimeContext = runtimeContext;
        this.entityType = entityType;
        this.entityModule = entityModule;
        this.moduleDirectory = moduleDirectory;
        this.absolutionPath = absolutionPath;
    }

    @Override
    public int hashCode() {
        return absolutionPath.hashCode() + entityModule.hashCode() + entityType.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EntityPathContext) {
            EntityPathContext other = (EntityPathContext) obj;
            return this.entityType == other.entityType &&
                   this.entityModule == other.entityModule &&
                   this.absolutionPath.equals(other.absolutionPath);
        }
        return false;
    }

    public boolean entityExist(String path) {
        ContextResult contextResult = findFileAndParentContext(path);
        EntityPathContext context = contextResult.getContext();
        String fileName = contextResult.getFileName();
        if (context.entityModule == EntityModule.SystemLibrary &&
                context.moduleDirectory == null) {
            path = context.absolutionPath + "/" + fileName;
            ClassLoader loader = getClass().getClassLoader();
            return loader.getResource(path) != null;
        } else {
            File file = new File(context.moduleDirectory, fileName);
            return file.exists() && file.isFile();
        }
    }

    public File entityFile(String path) {
        ContextResult contextResult = findFileAndParentContext(path);
        EntityPathContext context = contextResult.getContext();
        String fileName = contextResult.getFileName();
        if (context.entityModule == EntityModule.SystemLibrary &&
                context.moduleDirectory == null) {
            return null;
        } else {
            return new File(context.moduleDirectory, fileName);
        }
    }

    public InputStream inputStreamOfFile(String path) {
        ContextResult contextResult = findFileAndParentContext(path);
        EntityPathContext context = contextResult.getContext();
        String fileName = contextResult.getFileName();

        if (context.entityModule == EntityModule.SystemLibrary &&
            context.moduleDirectory == null) {
            String parentPath = context.absolutionPath;
            parentPath = parentPath.replaceAll("/$", "");
            path = parentPath + "/" + fileName;
            ClassLoader loader = getClass().getClassLoader();
            return loader.getResourceAsStream(path);
        } else {
            File file = new File(context.moduleDirectory, fileName);
            try {
                return new FileInputStream(file);
            } catch (FileNotFoundException e) {
                return null;
            }
        }
    }

    public ContextResult findFileAndParentContext(String path) {
        int beginIndexOfFileName = beginIndexOfFileNameWithPath(path);
        String contextPath = path.substring(0, beginIndexOfFileName);
        String fileName = path.substring(beginIndexOfFileName);
        if ("".equals(contextPath)) {
            contextPath = "./";
        }
        EntityPathContext context = findContext(contextPath);
        return new ContextResult(context, fileName);
    }

    private int beginIndexOfFileNameWithPath(String path) {
        path = path.replaceAll("(/|\\\\)$", "");
        return Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\')) + 1;
    }

    public EntityPathContext findContext(String path) {
        String absolutePath = getPathByThisContext(path);
        if (absolutePath.equals(this.absolutionPath)) {
            return this;
        }
        if (existDirectory(absolutePath, entityModule, moduleDirectory)) {
            return new EntityPathContext(runtimeContext,
                                         entityType, entityModule,
                                         moduleDirectory, absolutePath);
        }
        String[] components = PathUtil.splitComponents(absolutePath);
        if (components.length == 0) {
            return null;
        }
        String moduleName = components[0];
        File moduleDirectory = null;
        EntityModule module = null;

        if (moduleName.equals("lang") &&
            (EntityModule.Template == entityModule ||
            EntityModule.Library == entityModule)) {
            moduleDirectory = runtimeContext.getSystemEntityDirectory();
            module = EntityModule.SystemLibrary;
        } else if (!moduleName.equals("lang") &&
                    EntityModule.Template == entityModule) {
            moduleDirectory = runtimeContext.getLibrerayDirectoryMap().get(moduleName);
            module = EntityModule.Library;
        }
        if (moduleDirectory == null) {
            return null;
        }
        components = Arrays.copyOfRange(components, 0, components.length - 1);
        boolean isRoot = true;
        absolutePath = PathUtil.pathFromComponents(components, isRoot);
        return new EntityPathContext(runtimeContext,
                                     entityType, module,
                                     moduleDirectory, absolutePath);
    }

    private String getPathByThisContext(String path) {
        path = PathUtil.toUnixLikeStylePath(path);
        if (PathUtil.isAbsolutePath(path)) {
            return path;
        }
        String[] targetComponents = PathUtil.splitComponents(path);
        LinkedList<String> components = new LinkedList<>();
        for (String c : PathUtil.splitComponents(this.absolutionPath)) {
            components.add(c);
        }
        for (String tc : targetComponents) {
            if (tc.equals(".")) {

            } else if (tc.equals("..")) {
                if (components.isEmpty()) {
                    throw new EntityPathContextException("invalid path `"+ path + "`");
                }
                components.removeLast();
            } else {
                components.add(tc);
            }
        }
        boolean isRoot = true;
        path = PathUtil.pathFromComponents(
                components.toArray(new String[components.size()]), isRoot);
        return path;
    }

    private boolean existDirectory(String absolutionPath, EntityModule module, File moduleDirectory) {
        if (module == EntityModule.SystemLibrary && moduleDirectory == null) {
            //TODO check directory exist in JAR file.
            return false;
        } else {
            File directory = new File(moduleDirectory.getAbsolutePath(),
                                      entityType.getDirectoryName() + absolutionPath);
            return directory.exists() && directory.isDirectory();
        }
    }

    public static class ContextResult {

        private final EntityPathContext context;
        private final String fileName;

        private ContextResult(EntityPathContext context, String fileName) {
            this.context = context;
            this.fileName = fileName;
        }

        public EntityPathContext getContext() {
            return context;
        }

        public String getFileName() {
            return fileName;
        }

        @Override
        public int hashCode() {
            return context.hashCode() + fileName.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof ContextResult) {
                ContextResult other = (ContextResult) obj;
                return this.context.equals(other.context) &&
                       this.fileName.equals(other.fileName);
            }
            return false;
        }
    }

    public enum EntityModule {
        Template, SystemLibrary, Library
    }

    public enum EntityType {

        JavaScript("javascript"),
        Asset("asset"),
        Page("view");

        private final String directoryName;

        EntityType(String directoryName) {
            this.directoryName = directoryName;
        }
        public String getDirectoryName() {
            return directoryName;
        }
    }
}
