package com.rolandoislas.multihotbar.asm.transformer;

import com.rolandoislas.multihotbar.asm.FmlLoadingPlugin;
import com.rolandoislas.multihotbar.asm.ModContainer;
import com.rolandoislas.multihotbar.asm.util.ClassNodeUtil;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

public class InvTweaksConst implements IClassTransformer {
    private static final String CLASS_NAME = "invtweaks.InvTweaksConst";

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (!name.equals(CLASS_NAME) && !transformedName.equals(CLASS_NAME))
            return basicClass;
        FmlLoadingPlugin.LOGGER.debug(String.format("Transforming %s", CLASS_NAME));
        // Read bytecode
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(basicClass);
        classReader.accept(classNode, 0);
        // Transform
        transformHotbarSize(classNode);
        // Write to array
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        classNode.accept(classWriter);
        return classWriter.toByteArray();
    }

    private void transformHotbarSize(ClassNode classNode) {
        boolean found = false;
        for (FieldNode field : classNode.fields) {
            if (field.name.equals("INVENTORY_HOTBAR_SIZE") && field.desc.equals("I")) {
                found = true;
                field.value = ModContainer.HOTBAR_SIZE;
            }
        }
        if (!found)
            ClassNodeUtil.error("INVENTORY_HOTBAR_SIZE", classNode, CLASS_NAME);
    }
}
