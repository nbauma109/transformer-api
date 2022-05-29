package com.heliosdecompiler.transformerapi.decompilers.jadx;

import com.heliosdecompiler.transformerapi.common.SettingsApplicable;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import jadx.api.JadxArgs;

public class MapJadxArgs extends JadxArgs implements SettingsApplicable  {

    public MapJadxArgs(Map<String, String> settings) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        applySettings(settings);
    }
}
