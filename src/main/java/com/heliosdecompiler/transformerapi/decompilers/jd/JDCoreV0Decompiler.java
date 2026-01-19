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
package com.heliosdecompiler.transformerapi.decompilers.jd;

import org.apache.commons.lang3.time.StopWatch;

import com.heliosdecompiler.transformerapi.common.Loader;
import com.heliosdecompiler.transformerapi.decompilers.Decompiler;

import java.io.IOException;

import jd.core.DecompilationResult;
import jd.core.preferences.Preferences;
import jd.core.printer.PrinterImpl;
import jd.core.process.DecompilerImpl;

public class JDCoreV0Decompiler implements Decompiler<Preferences> {

    private static final DecompilerImpl DECOMPILER = new DecompilerImpl();

    private long time;

    @Override
    public DecompilationResult decompile(Loader loader, String internalName, Preferences preferences) throws IOException {
        StopWatch stopWatch = StopWatch.createStarted();

        // Init printer
        PrinterImpl printer = new PrinterImpl(preferences);
        String decompiledOutput = printer.buildDecompiledOutput(new JDLoader(loader), internalName, preferences, DECOMPILER);
        printer.getResult().setDecompiledOutput(decompiledOutput);

        time = stopWatch.getTime();
        
        return printer.getResult();
    }

    @Override
    public long getDecompilationTime() {
        return time;
    }

    @Override
    public String getDecompilerVersion() {
        return getAllManifestAttributes().getValue("jd-core-v0-version");
    }

    @Override
    public Preferences defaultSettings() {
        return new Preferences(JDSettings.defaults());
    }

    @Override
    public Preferences lineNumberSettings() {
        return new Preferences(JDSettings.lineNumbers());
    }
}
