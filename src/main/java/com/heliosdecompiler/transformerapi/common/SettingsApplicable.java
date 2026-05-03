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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

public interface SettingsApplicable {

    final class Constants {
        private static final Class<?>[] BOOLEAN_PARAM = { boolean.class };
        private static final Class<?>[] INT_PARAM = { int.class };

        private Constants() {
        }
    }

    default void applySettings(Map<String, String> settings) throws IllegalAccessException, InvocationTargetException {
        for (Method method : getClass().getMethods()) {
            if (method.getName().startsWith("set")) {
                String settingKey = method.getName().substring(3);
                if (Arrays.equals(method.getParameterTypes(), Constants.BOOLEAN_PARAM)) {
                    method.invoke(this, "true".equals(settings.get(settingKey)));
                } else if (Arrays.equals(method.getParameterTypes(), Constants.INT_PARAM)) {
                    method.invoke(this, Integer.parseInt(settings.getOrDefault(settingKey, "0")));
                }
            }
        }
    }
}
