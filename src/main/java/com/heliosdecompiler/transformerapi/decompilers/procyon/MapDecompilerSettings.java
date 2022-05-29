package com.heliosdecompiler.transformerapi.decompilers.procyon;

import com.heliosdecompiler.transformerapi.common.SettingsApplicable;
import com.strobel.decompiler.CommandLineOptions;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public class MapDecompilerSettings extends CommandLineOptions implements SettingsApplicable {

    public MapDecompilerSettings(Map<String, String> settings) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        applySettings(settings);
    }
}
