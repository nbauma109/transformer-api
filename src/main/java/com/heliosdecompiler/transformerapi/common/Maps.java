package com.heliosdecompiler.transformerapi.common;

import org.apache.commons.lang3.function.FailableFunction;

import java.util.Map;
import java.util.Objects;

public final class Maps {

    private Maps() {
    }

    public static <K, V, E extends Throwable> V computeIfAbsent(Map<K, V> map, K key, FailableFunction<? super K, ? extends V, E> mappingFunction) throws E {
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
