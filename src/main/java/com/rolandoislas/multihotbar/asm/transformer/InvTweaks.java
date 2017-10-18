package com.rolandoislas.multihotbar.asm.transformer;

import com.rolandoislas.multihotbar.asm.FmlLoadingPlugin;
import com.rolandoislas.multihotbar.asm.util.ClassNodeUtil;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.ListIterator;

public class InvTweaks implements IClassTransformer {
    private static final String CLASS_NAME = "invtweaks.InvTweaks";

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
        transformHandleRefill(classNode);
        // Write to array
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        classNode.accept(classWriter);
        return classWriter.toByteArray();
    }

    private void transformHandleRefill(ClassNode classNode) {
        boolean found = false;
        for (MethodNode method : classNode.methods) {
            if (method.name.equals("handleAutoRefill") && method.desc.equals("()V")) {
                ListIterator<AbstractInsnNode> instructionIterator = method.instructions.iterator();
                int getFoucusedSlotCall = 0;
                while (instructionIterator.hasNext()) {
                    AbstractInsnNode instruction = instructionIterator.next();
                    if (instruction instanceof IntInsnNode && instruction.getOpcode() == Opcodes.BIPUSH &&
                            ((IntInsnNode) instruction).operand == 27) {
                        found = true;
                        getFoucusedSlotCall = method.instructions.indexOf(instruction) - 1;
                        method.instructions.remove(method.instructions.get(getFoucusedSlotCall + 2));
                        method.instructions.remove(instruction);
                        break;
                    }
                }
                if (found) {
                    AbstractInsnNode getFoucusedSlotCallNode = method.instructions.get(getFoucusedSlotCall);
                    MethodInsnNode fixOffset = new MethodInsnNode(Opcodes.INVOKESTATIC,
                            "com/rolandoislas/multihotbar/asm/transformer/InvTweaks",
                            "fixOffset", "(I)I", false);
                    method.instructions.insert(getFoucusedSlotCallNode, fixOffset);
                }
                break;
            }
        }
        if (!found)
            ClassNodeUtil.error("handleAutoRefill()V", classNode, CLASS_NAME);
    }

    @SuppressWarnings("unused")
    public static int fixOffset(int focusedSlot) {
        if (focusedSlot < 9)
            return focusedSlot + 27;
        return focusedSlot - 9;
    }
}
