package com.heliosdecompiler.transformerapi.common;

import java.io.IOException;
import java.util.function.Predicate;

import name.falgout.jeffrey.throwing.ThrowingFunction;

/**
 * A loader which is agnostic of the decompiler implementation
 */
public class Loader {

    private final Predicate<String> canLoadFunction;
    private final ThrowingFunction<String, byte[], IOException> loadFunction;

    public Loader(Predicate<String> canLoadFunction, ThrowingFunction<String, byte[], IOException> loadFunction) {
        this.canLoadFunction = canLoadFunction;
        this.loadFunction = loadFunction;
    }

    public boolean canLoad(String internalName) {
        return canLoadFunction.test(internalName);
    }
    
    public byte[] load(String internalName) throws IOException {
        return loadFunction.apply(internalName);
    }
}
