package com.heliosdecompiler.transformerapi.common;

import com.heliosdecompiler.transformerapi.decompilers.procyon.ProcyonFastTypeLoader;
import com.strobel.decompiler.DecompilerSettings;
import com.strobel.decompiler.PlainTextOutput;
import com.strobel.decompiler.languages.Language;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

public abstract class ProcyonTask {
    
    protected abstract Language language();

    public DecompilerSettings defaultSettings() {
        DecompilerSettings decompilerSettings = new DecompilerSettings();
        decompilerSettings.setLanguage(language());
        decompilerSettings.setForceExplicitImports(true);
        return decompilerSettings;
    }
    
    protected String process(Loader loader, String internalName, DecompilerSettings settings) throws IOException {
        Map<String, byte[]> importantClasses = new HashMap<>();
        importantClasses.put(internalName, loader.load(internalName));
        if (settings.getTypeLoader() == null) {
            settings.setTypeLoader(new ProcyonFastTypeLoader(importantClasses, loader));
        }
        StringWriter stringwriter = new StringWriter();
        com.strobel.decompiler.Decompiler.decompile(internalName, new PlainTextOutput(stringwriter), settings);
        return stringwriter.toString();
    }
}
