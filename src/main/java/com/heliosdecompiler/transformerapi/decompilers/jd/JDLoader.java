package com.heliosdecompiler.transformerapi.decompilers.jd;

import com.heliosdecompiler.transformerapi.common.Loader;

import java.io.IOException;

public final class JDLoader implements org.jd.core.v1.api.loader.Loader {

    private final Loader loader;

    public JDLoader(Loader loader) {
        this.loader = loader;
    }

    @Override
    public byte[] load(String internalName) throws IOException {
        return loader.load(internalName);
    }

    @Override
    public boolean canLoad(String internalName) {
        return loader.canLoad(internalName);
    }
}