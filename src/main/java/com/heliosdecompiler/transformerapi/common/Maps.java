package com.heliosdecompiler.transformerapi.common;

import java.util.Map;
import java.util.Objects;

import name.falgout.jeffrey.throwing.ThrowingFunction;

public final class Maps {

    private Maps() {
    }

    public static <K, V, E extends Throwable> V computeIfAbsent(Map<K, V> map, K key, ThrowingFunction<? super K, ? extends V, E> mappingFunction) throws E {
        Objects.requireNonNull(mappingFunction);
        @SuppressWarnings("all")
        V v = map.get(key);
        if (v == null) {
            V newValue;
            if ((newValue = mappingFunction.apply(key)) != null) {
                map.put(key, newValue);
                return newValue;
            }
        }
        return v;
    }
}
