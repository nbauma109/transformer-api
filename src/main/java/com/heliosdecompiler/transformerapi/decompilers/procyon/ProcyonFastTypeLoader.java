package com.heliosdecompiler.transformerapi.decompilers.procyon;

import com.heliosdecompiler.transformerapi.common.Loader;
import com.strobel.assembler.metadata.Buffer;
import com.strobel.assembler.metadata.ITypeLoader;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ProcyonFastTypeLoader implements ITypeLoader {

    private final Map<String, byte[]> importantData = new HashMap<>();
    private final Loader loader;

    public ProcyonFastTypeLoader(Map<String, byte[]> importantClasses, Loader loader) {
        this.importantData.putAll(importantClasses);
        this.loader = loader;
    }

    @Override
    public boolean tryLoadType(String s, Buffer buffer) {
        if (importantData.containsKey(s)) {
            byte[] data = importantData.get(s);
            buffer.putByteArray(data, 0, data.length);
            buffer.position(0);
            return true;
        } else {
            if (loader.canLoad(s)) {
                try {
                    byte[] data = loader.load(s);
                    buffer.putByteArray(data, 0, data.length);
                    buffer.position(0);
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            }
            return false;
        }
    }
}