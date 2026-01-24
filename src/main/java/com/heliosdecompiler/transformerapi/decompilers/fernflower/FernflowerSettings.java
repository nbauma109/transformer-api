/*
 * Copyright 2017 Sam Sun <github-contact@samczsun.com>
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

package com.heliosdecompiler.transformerapi.decompilers.fernflower;

import java.util.HashMap;
import java.util.Map;

import static org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences.BYTECODE_SOURCE_MAPPING;
import static org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences.DECOMPILE_GENERIC_SIGNATURES;
import static org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences.DUMP_ORIGINAL_LINES;
import static org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences.REMOVE_SYNTHETIC;

/**
 * Represents settings which can be used to configure the particular decompiling session.
 * <p>
 * Since Fernflower has its own internal settings layout, that will be used here. See {@link org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences}
 */
public class FernflowerSettings {
    private Map<String, Object> internalSettings;

    public FernflowerSettings() {
        this.internalSettings = new HashMap<>();
    }

    public FernflowerSettings(Map<String, String> internalSettings) {
        this.internalSettings = new HashMap<>(internalSettings);
    }

    /**
     * Set the given key to the given value. Keys can be found in {@link org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences}
     *
     * @return The same instance, for chaining
     */
    public FernflowerSettings set(String key, Object value) {
        this.internalSettings.put(key, value);
        return this;
    }

    public Map<String, Object> getSettings() {
        return this.internalSettings;
    }

    public static Map<String, String> defaults() {
        return Map.of(REMOVE_SYNTHETIC, "1", DECOMPILE_GENERIC_SIGNATURES, "1");
    }

    public static Map<String, String> lineNumbers() {
        return Map.of(REMOVE_SYNTHETIC, "1", DECOMPILE_GENERIC_SIGNATURES, "1", DUMP_ORIGINAL_LINES, "1", BYTECODE_SOURCE_MAPPING, "1");
    }
}
