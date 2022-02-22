/*******************************************************************************
 * Copyright (C) 2022 GPLv3
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package com.heliosdecompiler.transformerapi.common;

import com.heliosdecompiler.transformerapi.decompilers.procyon.ProcyonFastTypeLoader;
import com.strobel.Procyon;
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
        decompilerSettings.setOutputFileHeaderText("\nDecompiled by Procyon v" + Procyon.version() + "\n");
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
