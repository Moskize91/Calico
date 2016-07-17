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
                             EntityType entityType,
                             EntityModule entityModule,
                             String absolutionPath) {
        this.runtimeContext = runtimeContext;
        this.entityType = entityType;
        this.entityModule = entityModule;
        this.absolutionPath = absolutionPath;
        this.moduleDirectory = getModuleDirectory(runtimeContext,
                                                  entityType,
                                                  entityModule);
    }

    private EntityPathContext(EntityPathContext parentContext,
                              String absolutionPath) {
        this.runtimeContext = parentContext.runtimeContext;
        this.entityType = parentContext.entityType;
        this.entityModule = parentContext.entityModule;
        this.moduleDirectory = parentContext.moduleDirectory;
        this.absolutionPath = absolutionPath;
    }

    private File getModuleDirectory(RuntimeContext runtimeContext,
                                    EntityType entityType,
                                    EntityModule entityModule) {
        File routerDirectory = null;
        if (entityModule == EntityModule.SystemLibrary) {
            routerDirectory = runtimeContext.getSystemEntityDirectory();
        } else if (entityModule == EntityModule.Library) {
            //TODO not implements
        } else if (entityModule == EntityModule.Template) {
            routerDirectory = runtimeContext.getTemplateDirectory();
        }
        return new File(routerDirectory, entityType.getDirectoryName());
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
        String absolutePath = absolutionPathOfThis(path);
        if (absolutePath.equals(this.absolutionPath)) {
            return this;
        }
        if (existDirectory(absolutePath, entityModule, moduleDirectory)) {
            return new EntityPathContext(this, absolutePath);
        }
        String[] components = PathUtil.splitComponents(absolutePath);
        if (components.length == 0) {
            return null;
        }
        String moduleName = components[0];
        EntityModule module = null;

        if (moduleName.equals("system") &&
           (EntityModule.Template == entityModule ||
            EntityModule.Library == entityModule)
        ) {
            module = EntityModule.SystemLibrary;
        } else if (!moduleName.equals("system") &&
                    EntityModule.Template == entityModule) {
            module = EntityModule.Library;
        }
        if (moduleDirectory == null) {
            return null;
        }
        components = Arrays.copyOfRange(components, 0, components.length - 1);
        boolean isRoot = true;
        absolutePath = PathUtil.pathFromComponents(components, isRoot);
        return new EntityPathContext(runtimeContext, entityType,
                                     module, absolutePath);
    }

    public String absolutionPathOfThis(String relativePath) {
        String mergedPath = PathUtil.pathMerge(this.absolutionPath, relativePath);
        if (mergedPath == null) {
            throw new EntityPathContextException("invalid path `"+ relativePath + "`");
        }
        return mergedPath;
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
