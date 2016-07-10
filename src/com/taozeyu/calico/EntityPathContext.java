package com.taozeyu.calico;

import com.taozeyu.calico.exception.EntityPathContextException;
import com.taozeyu.calico.util.PathUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
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

    private EntityPathContext(RuntimeContext runtimeContext,
                              EntityType entityType, EntityModule entityModule,
                              File moduleDirectory, String absolutionPath) {
        this.runtimeContext = runtimeContext;
        this.entityType = entityType;
        this.entityModule = entityModule;
        this.moduleDirectory = moduleDirectory;
        this.absolutionPath = absolutionPath;
    }

    public boolean fileExist(String path) {
        EntityPathContext context = contextOfFile(path);
        String fileName = fileName(path);
        if (context.entityModule == EntityModule.SystemLibrary &&
                context.moduleDirectory == null) {
            path = "/lang"+ context.absolutionPath + "/" + fileName;
            ClassLoader loader = getClass().getClassLoader();
            return loader.getResource(path) != null;
        } else {
            File file = new File(context.moduleDirectory, fileName);
            return file.exists() && file.isFile();
        }
    }

    public InputStream inputStreamOfFile(String path) {
        EntityPathContext context = contextOfFile(path);
        String fileName = fileName(path);
        if (context.entityModule == EntityModule.SystemLibrary &&
            context.moduleDirectory == null) {
            path = "/lang"+ context.absolutionPath + "/" + fileName;
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

    public EntityPathContext contextOfFile(String path) {
        int beginIndexOfFileName = beginIndexOfFileNameWithPath(path);
        String contextPath = path.substring(0, beginIndexOfFileName);
        return this.context(contextPath);
    }

    private String fileName(String path) {
        return path.substring(beginIndexOfFileNameWithPath(path));
    }

    private int beginIndexOfFileNameWithPath(String path) {
        path = path.replaceAll("(/|\\\\)$", "");
        return Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\')) + 1;
    }

    public EntityPathContext context(String path) {
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

        if (moduleName.equals("calico") &&
            (EntityModule.Template == entityModule ||
            EntityModule.Library == entityModule)) {
            moduleDirectory = runtimeContext.getSystemEntityDirectory();
            module = EntityModule.SystemLibrary;
        } else if (!moduleName.equals("calico") &&
                    EntityModule.Template == entityModule) {
            moduleDirectory = runtimeContext.getLibrerayDirectoryMap().get(moduleName);
            module = EntityModule.Library;
        }
        if (moduleDirectory == null) {
            return null;
        }
        components = Arrays.copyOfRange(components, 1, components.length - 1);
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

    public enum EntityModule {
        Template, SystemLibrary, Library
    }

    public enum EntityType {

        JavaScript("javascript", new String[]{"js"});

        private final Set<String> extensionNameSet = new HashSet<>();
        private final String extensionName;
        private final String directoryName;

        EntityType(String directoryName, String[] extensionNames) {
            for (String extensionName : extensionNames) {
                this.extensionNameSet.add(extensionName.toLowerCase());
            }
            this.directoryName = directoryName;
            this.extensionName = extensionNames[0];
        }

        public String getExtensionName() {
            return extensionName;
        }

        public String getDirectoryName() {
            return directoryName;
        }

        public boolean matchExtensionName(String extensionName) {
            if (extensionName == null) {
                return false;
            }
            return extensionNameSet.contains(extensionName.toLowerCase());
        }
    }
}
