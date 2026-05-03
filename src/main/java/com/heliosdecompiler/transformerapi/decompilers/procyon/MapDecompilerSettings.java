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

package com.heliosdecompiler.transformerapi.decompilers.procyon;

import com.heliosdecompiler.transformerapi.common.SettingsApplicable;
import com.strobel.decompiler.CommandLineOptions;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public class MapDecompilerSettings extends CommandLineOptions implements SettingsApplicable {

    public static final String INCLUDE_LINE_NUMBERS = "IncludeLineNumbers";
    public static final String RAW_BYTECODE = "RawBytecode";
    public static final String SUPPRESS_BANNER = "SuppressBanner";
    public static final String STRETCH_LINES = "StretchLines";

    public MapDecompilerSettings(Map<String, String> settings) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        applySettings(settings);
    }

    public static Map<String, String> defaults() {
        return Map.of(SUPPRESS_BANNER, "true");
    }

    public static Map<String, String> byteCodeSettings() {
        return Map.of(SUPPRESS_BANNER, "true", RAW_BYTECODE, "true");
    }

    public static Map<String, String> lineNumbers() {
        return Map.of(SUPPRESS_BANNER, "true", INCLUDE_LINE_NUMBERS, "true", STRETCH_LINES, "false");
    }
}
