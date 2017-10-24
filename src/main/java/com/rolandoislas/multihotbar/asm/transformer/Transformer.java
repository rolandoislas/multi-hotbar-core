package com.rolandoislas.multihotbar.asm.transformer;

import com.rolandoislas.multihotbar.asm.FmlLoadingPlugin;
import com.rolandoislas.multihotbar.asm.util.ItemNotFoundException;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.ListIterator;

import static org.apache.logging.log4j.core.impl.ThrowableFormatOptions.CLASS_NAME;

public class Transformer implements IClassTransformer {
    private static final int HOTBAR_SIZE = 36;
    private static final int VANILLA_HOTBAR_SIZE = 9;

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        // InventoryPlayer
        String className = "net.minecraft.entity.player.InventoryPlayer";
        if (doesAnyStringEqual(className, name, transformedName)) {
            ClassNode classNode = byteArrayToClassNode(basicClass);
            try {
                transformMethodBipush("getHotbarSize", "()I",
                        "j", "()I",
                        VANILLA_HOTBAR_SIZE, HOTBAR_SIZE, classNode);
                transformMethodBipush("getBestHotbarSlot", "()I",
                        "l", "()I",
                        VANILLA_HOTBAR_SIZE, HOTBAR_SIZE, classNode);
                transformMethodBipush("isHotbar", "(I)Z",
                        "e", "(I)Z",
                        VANILLA_HOTBAR_SIZE, HOTBAR_SIZE, classNode);
            }
            catch (ItemNotFoundException e) {
                e.setClassName(className);
                throw e;
            }
            return classNodeToByteArray(classNode, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        }
        // ForgeHooks
        className = "net.minecraftforge.common.ForgeHooks";
        if (doesAnyStringEqual(className, name, transformedName)) {
            ClassNode classNode = byteArrayToClassNode(basicClass);
            try {
                transformPickBlock(classNode);
            }
            catch (ItemNotFoundException e) {
                e.setClassName(className);
                throw e;
            }
            return classNodeToByteArray(classNode, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        }
        // InvTweaksConst
        className = "invtweaks.InvTweaksConst";
        if (doesAnyStringEqual(className, name, transformedName)) {
            ClassNode classNode = byteArrayToClassNode(basicClass);
            try {
                transformFieldToExtendedHotbar("HOTBAR_SIZE", "I",
                        "", "", classNode);
                transformFieldToExtendedHotbar("INVENTORY_HOTBAR_SIZE", "I",
                        "", "", classNode);
            }
            catch (ItemNotFoundException e) {
                e.setClassName(className);
                throw e;
            }
            return classNodeToByteArray(classNode, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        }
        // InvTweaks
        className = "invtweaks.InvTweaks";
        if (doesAnyStringEqual(className, name, transformedName)) {
            ClassNode classNode = byteArrayToClassNode(basicClass);
            try {
                transformHandleRefill(classNode);
            }
            catch (ItemNotFoundException e) {
                e.setClassName(className);
                throw e;
            }
            return classNodeToByteArray(classNode, ClassWriter.COMPUTE_MAXS);
        }
        return basicClass;
    }

    /**
     * Write a class node to a byte array
     * @param classNode node to write
     * @param flags class writer flags
     * @return bytes of class
     */
    private byte[] classNodeToByteArray(ClassNode classNode, int flags) {
        ClassWriter classWriter = new ClassWriter(flags);
        classNode.accept(classWriter);
        return classWriter.toByteArray();
    }

    /**
     * Convert a byte array to a class node
     * @param classByteCode class byte code
     * @return class node
     */
    private ClassNode byteArrayToClassNode(byte[] classByteCode) {
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(classByteCode);
        classReader.accept(classNode, 0);
        return classNode;
    }

    /**
     * Check if the the first string passed matches any other passed string
     * @param stringToMatch main string to match against
     * @param otherStrings any other string to match the first
     * @return if one of the other string match
     */
    private boolean doesAnyStringEqual(String stringToMatch, String... otherStrings) {
        for (String otherString : otherStrings)
            if (otherString.equals(stringToMatch))
                return true;
        return false;
    }

    /**
     * Replace all instanced of a BIPUSH with a specific number to a different number in the provided method in the
     * class node
     * @param methodName non-obfuscated method name
     * @param methodDescription non-obfuscated method description
     * @param methodNameObfuscated obfuscated method name
     * @param descriptionObfuscated obfuscated method description
     * @param from the number of BIPUSH to replace
     * @param to the number to update the BIPUSH with
     * @param classNode class node to operate on
     * @throws ItemNotFoundException method was not found
     */
    private void transformMethodBipush(String methodName, String methodDescription,
                                                        String methodNameObfuscated, String descriptionObfuscated,
                                                        int from, int to, ClassNode classNode)
            throws ItemNotFoundException {
        boolean found = false;
        for (MethodNode method : classNode.methods) {
            if ((method.name.equals(methodName) || method.name.equals(methodNameObfuscated)) &&
                    (method.desc.equals(methodDescription) || method.desc.equals(descriptionObfuscated))) {
                FmlLoadingPlugin.LOGGER.debug(String.format("Transforming %s#%s%s",
                        CLASS_NAME, methodName, methodDescription));
                // Create new instructions list
                InsnList instructions = new InsnList();
                ListIterator<AbstractInsnNode> instructionIterator = method.instructions.iterator();
                while (instructionIterator.hasNext()) {
                    AbstractInsnNode instruction = instructionIterator.next();
                    if (instruction instanceof IntInsnNode && instruction.getOpcode() == Opcodes.BIPUSH &&
                            ((IntInsnNode) instruction).operand == from) {
                        found = true;
                        ((IntInsnNode) instruction).operand = to;
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
            throw new ItemNotFoundException(methodName, methodDescription, ItemNotFoundException.TYPE.METHOD,
                    classNode);
    }

    /**
     * Set a fields value to the extended hotbar size. The field should of the type Integer
     * @param fieldName name of field to transform
     * @param description description of field
     * @param fieldNameObfuscated obfuscated field name
     * @param descriptionObfuscated obfuscated description
     * @param classNode class node to search in
     */
    private void transformFieldToExtendedHotbar(String fieldName, String description, String fieldNameObfuscated,
                                                String descriptionObfuscated, ClassNode classNode) {
        boolean found = false;
        for (FieldNode field : classNode.fields) {
            if ((field.name.equals(fieldName) || field.name.equals(fieldNameObfuscated)) &&
                    (field.desc.equals(description) || field.desc.equals(descriptionObfuscated))) {
                found = true;
                field.value = HOTBAR_SIZE;
            }
        }
        if (!found)
            throw new ItemNotFoundException(fieldName, description, ItemNotFoundException.TYPE.FIELD, classNode);
    }

    /**
     * Transform inventory tweaks' auto refill offset. It expends the current item to be 0-8 and adds 27 to it.
     * This patch replaces the 27 to a static method call that return the appropriate offset.
     * @see  Transformer#fixOffsetHandleRefill(int)
     * @param classNode class node to search for method
     */
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
                            "com/rolandoislas/multihotbar/asm/transformer/Transformer",
                            "fixOffsetHandleRefill", "(I)I", false);
                    method.instructions.insert(getFoucusedSlotCallNode, fixOffset);
                }
                break;
            }
        }
        if (!found)
            throw new ItemNotFoundException("handleAutoRefill", "()V", ItemNotFoundException.TYPE.METHOD, classNode);
    }

    /**
     * Helper function that changes a "currentItem" offset to an inventory offset [0-35].
     * O-8 is returned as 27-35
     * 9-35 is return as 0-26
     * @see Transformer#transformHandleRefill(ClassNode)
     * @param focusedSlot 0-35 InventoryPlayer "currentItem"
     */
    @SuppressWarnings({"WeakerAccess", "unused"})
    public static int fixOffsetHandleRefill(int focusedSlot) {
        if (focusedSlot < 9)
            return focusedSlot + 27;
        return focusedSlot - 9;
    }

    /**
     * Transform ForgeHooks's onPickBlock offset. It only expects 0-8 as the current item and adds 36.
     * This patch replaces the offset with a static method call that return the correct offset.
     * @see Transformer#fixOffsetPickBlock(int)
     * @param classNode
     */
    private void transformPickBlock(ClassNode classNode) {
        String name = "onPickBlock";
        String nameObf = "onPickBlock";
        String desc = "(Lnet/minecraft/util/math/RayTraceResult;Lnet/minecraft/entity/player/EntityPlayer;" +
                "Lnet/minecraft/world/World;)Z";
        String descObf = "(Lbdu;Laay;Lajs;)Z";
        // Transform 36 to zero before adding the offset call
        transformMethodBipush(name, desc, nameObf, descObf, 36, 0, classNode);
        // Add the offset method call
        boolean found = false;
        for (MethodNode method : classNode.methods) {
            if ((method.name.equals(name) || method.name.equals(nameObf)) &&
                    (method.desc.equals(desc) || method.desc.equals(descObf))) {
                ListIterator<AbstractInsnNode> instructionIterator = method.instructions.iterator();
                while (instructionIterator.hasNext()) {
                    AbstractInsnNode instruction = instructionIterator.next();
                    if (instruction instanceof IntInsnNode && instruction.getOpcode() == Opcodes.BIPUSH &&
                            ((IntInsnNode) instruction).operand == 0) {
                        found = true;
                        AbstractInsnNode currentItem = method.instructions.get(
                                method.instructions.indexOf(instruction) + 3);
                        method.instructions.insert(currentItem, new MethodInsnNode(
                                Opcodes.INVOKESTATIC,
                                "com/rolandoislas/multihotbar/asm/transformer/Transformer",
                                "fixOffsetPickBlock", "(I)I", false
                        ));
                        break;
                    }
                }
                break;
            }
        }
        if (!found)
            throw new ItemNotFoundException(name, desc, ItemNotFoundException.TYPE.METHOD, classNode);
    }

    /**
     * Helper function that changes a "currentItem" to an index of 9-44.
     * O-8 is returned as 36-44
     * 9-35 is return as 9-35
     * @see Transformer#transformPickBlock(ClassNode)
     * @param currentItem 0-35 InventoryPlayer "currentItem"
     */
    @SuppressWarnings({"WeakerAccess", "unused"})
    public static int fixOffsetPickBlock(int currentItem) {
        if (currentItem < 9)
            return currentItem + 36;
        return currentItem;
    }
}
