package com.rolandoislas.multihotbar.asm.transformer;

import com.rolandoislas.multihotbar.asm.FmlLoadingPlugin;
import com.rolandoislas.multihotbar.asm.ModContainer;
import com.rolandoislas.multihotbar.asm.util.ClassNodeUtil;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.ListIterator;

public class InventoryPlayer implements IClassTransformer {
    private static final String CLASS_NAME = "net.minecraft.entity.player.InventoryPlayer";

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
        transform9to36("getHotbarSize", "()I", "i", "()I", classNode);
        transform9to36("getCurrentItem", "()Lnet/minecraft/item/ItemStack;",
                "h", "()Ladd;", classNode);
        // Write to array
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        classNode.accept(classWriter);
        return classWriter.toByteArray();
    }

    private void transform9to36(String methodName, String methodDescription, String methodNameObfuscaed,
                                String descriptionObfuscated, ClassNode classNode) {
        boolean found = false;
        for (MethodNode method : classNode.methods) {
            if ((method.name.equals(methodName) || method.name.equals(methodNameObfuscaed)) &&
                    (method.desc.equals(methodDescription) || method.desc.equals(descriptionObfuscated))) {
                FmlLoadingPlugin.LOGGER.debug(String.format("Transforming %s#%s%s",
                        CLASS_NAME, methodName, methodDescription));
                // Create new instructions list
                InsnList instructions = new InsnList();
                ListIterator<AbstractInsnNode> instructionIterator = method.instructions.iterator();
                while (instructionIterator.hasNext()) {
                    AbstractInsnNode instruction = instructionIterator.next();
                    if (instruction instanceof IntInsnNode && instruction.getOpcode() == Opcodes.BIPUSH &&
                            ((IntInsnNode) instruction).operand == ModContainer.VANILLA_HOTBAR_SIZE) {
                        found = true;
                        ((IntInsnNode) instruction).operand = ModContainer.HOTBAR_SIZE;
                    }
                    instructions.add(instruction);
                }
                // Replace old instruction list
                method.instructions.clear();
                method.instructions.add(instructions);
                break;
            }
        }
        if (!found)
            ClassNodeUtil.error(String.format("%s%s", methodName, methodDescription), classNode, CLASS_NAME);
    }
}
