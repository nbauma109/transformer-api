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
