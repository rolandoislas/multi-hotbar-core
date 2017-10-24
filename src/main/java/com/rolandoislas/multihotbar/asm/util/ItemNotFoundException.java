package com.rolandoislas.multihotbar.asm.util;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Locale;

public class ItemNotFoundException extends RuntimeException {
    private final String name;
    private final String description;
    private final TYPE type;
    private final ClassNode classNode;
    private String className;

    public ItemNotFoundException(String name, String description, TYPE type, ClassNode classNode) {
        super();
        this.name = name;
        this.description = description;
        this.type = type;
        this.classNode = classNode;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public enum TYPE {FIELD, METHOD}

    @Override
    public String getMessage() {
        StringBuilder message = new StringBuilder();
        message.append(String.format("Could not find %s in class %s: %s %s\n",
                type.name().toLowerCase(Locale.US),
                className,
                name,
                description
        ));
        message.append("Fields\n----------\n");
        for (FieldNode fieldNode : classNode.fields)
            message.append(String.format("%s %s\n", fieldNode.name, fieldNode.desc));
        message.append("Methods\n----------\n");
        for (MethodNode methodNode : classNode.methods)
            message.append(String.format("%s#%s\n", methodNode.name, methodNode.desc));
        return message.toString();
    }
}
