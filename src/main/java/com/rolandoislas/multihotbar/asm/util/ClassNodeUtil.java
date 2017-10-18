package com.rolandoislas.multihotbar.asm.util;

import com.rolandoislas.multihotbar.asm.FmlLoadingPlugin;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class ClassNodeUtil {
    public static void error(String methodName, ClassNode classNode, String className) {
        FmlLoadingPlugin.LOGGER.error(String.format("Could not find %s#%s",
                className, methodName));
        for (MethodNode method : classNode.methods)
            FmlLoadingPlugin.LOGGER.debug(method.name + " " + method.desc);
        FMLCommonHandler.instance().exitJava(1, false);
    }
}
