package com.heliosdecompiler.transformerapi.decompilers.jadx;

import com.heliosdecompiler.transformerapi.common.SettingsApplicable;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import jadx.api.JadxArgs;

public class MapJadxArgs extends JadxArgs implements SettingsApplicable  {

    public MapJadxArgs(Map<String, String> settings) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        applySettings(settings);
    }

    public static Map<String, String> defaults() {
        return Map.of("MoveInnerClasses", "true");
    }

    public static Map<String, String> lineNumbers() {
        return Map.of("InsertDebugLines", "true", "DebugInfo", "true", "MoveInnerClasses", "true");
    }
    
}
