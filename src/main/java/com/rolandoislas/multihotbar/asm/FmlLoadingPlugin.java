package com.rolandoislas.multihotbar.asm;

import com.rolandoislas.multihotbar.asm.transformer.Transformer;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.Map;

@IFMLLoadingPlugin.TransformerExclusions({"com.rolandoislas.multihotbar.asm",
        "com.rolandoislas.multihotbar.asm.transformer"})
public class FmlLoadingPlugin implements IFMLLoadingPlugin {
    public static final Logger LOGGER;

    static {
        LOGGER = LogManager.getLogger(ModContainer.MOD_ID);
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[] {
                Transformer.class.getName()
        };
    }

    @Override
    public String getModContainerClass() {
        return ModContainer.class.getName();
    }

    @Nullable
    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {

    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
