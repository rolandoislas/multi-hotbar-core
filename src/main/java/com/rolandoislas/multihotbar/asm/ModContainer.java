package com.rolandoislas.multihotbar.asm;

import com.google.common.eventbus.EventBus;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.ModMetadata;

import javax.annotation.Nullable;
import java.security.cert.Certificate;

public class ModContainer extends DummyModContainer {
    static final String MOD_ID = "multihotbarcore";

    public ModContainer() {
        super(getMetaData());
    }

    private static ModMetadata getMetaData() {
        ModMetadata meta = new ModMetadata();
        meta.modId = MOD_ID;
        meta.name = "Multi-Hotbar Core";
        meta.description = "Core mod for Multi-Hotbar";
        meta.version = "@VERSION@";
        meta.url = "https://github.com/rolandoislas/multi-hotbar-core";
        meta.authorList.add("rolandoislas");
        return meta;
    }

    @Override
    public boolean registerBus(EventBus bus, LoadController controller) {
        return true;
    }

    @Nullable
    @Override
    public Certificate getSigningCertificate() {
        Certificate[] certs = getClass().getProtectionDomain().getCodeSource().getCertificates();
        return certs != null && certs.length > 0 ? certs[0] : null;
    }
}