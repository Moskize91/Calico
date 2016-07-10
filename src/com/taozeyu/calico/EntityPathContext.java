package com.taozeyu.calico;

import com.taozeyu.calico.exception.EntityPathContextException;
import com.taozeyu.calico.util.PathUtil;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * Created by taozeyu on 16/7/3.
 */
public class EntityPathContext {

    private static final EntityPathContext javascriptRootEntityPathContenxt =
            new EntityPathContext(EntityType.JavaScript, "/");

    private final EntityType entityType;
    private final String absolutionPath;

    private EntityPathContext(EntityType entityType, String absolutionPath) {
        this.entityType = entityType;
        this.absolutionPath = absolutionPath;
    }

    public EntityPathContext context(String path) {
        path = PathUtil.toUnixLikeStylePath(path);
        String extensionName = PathUtil.getExtensionName(path, null);
        if (entityType.matchExtensionName(extensionName)) {
            path = PathUtil.clearExtensionName(path);
        }
        if (PathUtil.isAbsolutePath(path)) {
            return new EntityPathContext(this.entityType, path);
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
        return new EntityPathContext(this.entityType, path);
    }

    public enum EntityType {
        JavaScript("js", "javascript");

        private final Set<String> extensionNameSet = new HashSet<>();
        EntityType(String...extensionNames) {
            for (String extensionName : extensionNames) {
                extensionNameSet.add(extensionName.toLowerCase());
            }
        }

        public boolean matchExtensionName(String extensionName) {
            if (extensionName == null) {
                return false;
            }
            return extensionNameSet.contains(extensionName.toLowerCase());
        }
    }
}
