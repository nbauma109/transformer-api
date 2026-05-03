/*
 * © 2022-2025 Nicolas Baumann (@nbauma109)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
            V newValue = mappingFunction.apply(key);
            if (newValue != null) {
                map.put(key, newValue);
                return newValue;
            }
        }
        return v;
    }
}
