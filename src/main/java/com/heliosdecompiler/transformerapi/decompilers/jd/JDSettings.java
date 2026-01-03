/*
 * Copyright 2026 Apache License, Version 2.0
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

package com.heliosdecompiler.transformerapi.decompilers.jd;

import java.util.Map;

import static jd.core.preferences.Preferences.WRITE_LINE_NUMBERS;
import static jd.core.preferences.Preferences.WRITE_METADATA;

public class JDSettings {

    private static final String TRUE = "true";
    private static final String FALSE = "false";

    private JDSettings() {
    }

    public static Map<String, String> defaults() {
        return Map.of(WRITE_LINE_NUMBERS, FALSE, WRITE_METADATA, FALSE);
    }

    public static Map<String, String> lineNumbers() {
        return Map.of(WRITE_LINE_NUMBERS, TRUE, WRITE_METADATA, FALSE);
    }

}
