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
