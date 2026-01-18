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

package com.heliosdecompiler.transformerapi.decompilers.vineflower;

import org.vineflower.java.decompiler.main.extern.IFernflowerPreferences;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents settings which can be used to configure the particular decompiling session.
 * <p>
 * Since Fernflower has its own internal settings layout, that will be used here. See {@link org.vineflower.java.decompiler.main.extern.IFernflowerPreferences}
 */
public class VineflowerSettings {
    private Map<String, Object> internalSettings;

    public VineflowerSettings() {
        this.internalSettings = new HashMap<>();
    }

    public VineflowerSettings(Map<String, String> internalSettings) {
        this.internalSettings = new HashMap<>(internalSettings);
        for (String key : internalSettings.keySet()) {
            // Ignore options specified in legacy format.
            // This both allows concurrent unique Fernflower and Vineflower options,
            // and it fixes an issue where Fernflower options would override those of Vineflower's.
            if (key.length() == 3) {
                this.internalSettings.remove(key);
            }
        }
    }

    /**
     * Set the given key to the given value. Built-in keys can be found in {@link org.vineflower.java.decompiler.main.extern.IFernflowerPreferences},
     * and all options can be accessed with {@link org.vineflower.java.decompiler.api.DecompilerOption#getAll()}
     *
     * @return The same instance, for chaining
     */
    public VineflowerSettings set(String key, Object value) {
        this.internalSettings.put(key, value);
        return this;
    }

    public Map<String, Object> getSettings() {
        return this.internalSettings;
    }
    
    public static Map<String, String> lineNumbers() {
        return Map.of(IFernflowerPreferences.DUMP_ORIGINAL_LINES, "1", IFernflowerPreferences.BYTECODE_SOURCE_MAPPING, "1");
    }
}
