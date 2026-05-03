/*
 * © 2022-2026 Nicolas Baumann (@nbauma109)
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
package com.heliosdecompiler.transformerapi.decompilers.jadx;

import com.heliosdecompiler.transformerapi.common.SettingsApplicable;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import jadx.api.JadxArgs;

public class MapJadxArgs extends JadxArgs implements SettingsApplicable  {

    private static final Map<String, String> COMMON_OPTIONS = Map.of(
            "MoveInnerClasses", "true",
            "InlineAnonymousClasses", "true",
            "UseImports", "true"
    );

    public MapJadxArgs(Map<String, String> settings) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        applySettings(settings);
    }

    public static Map<String, String> defaults() {
        return COMMON_OPTIONS;
    }

    public static Map<String, String> lineNumbers() {
        Map<String, String> options = new HashMap<>(COMMON_OPTIONS);
        options.put("InsertDebugLines", "true");
        options.put("DebugInfo", "true");
        return Collections.unmodifiableMap(options);
    }
}
