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

import com.heliosdecompiler.transformerapi.TransformationException;
import com.heliosdecompiler.transformerapi.common.Loader;
import com.heliosdecompiler.transformerapi.decompilers.Decompiler;

import java.io.IOException;

import jd.core.preferences.Preferences;
import jd.core.printer.PlainTextPrinter;
import jd.core.process.DecompilerImpl;

public class JDCoreV0Decompiler implements Decompiler<Preferences> {

    private static final DecompilerImpl DECOMPILER = new DecompilerImpl();

    @Override
    public String decompile(Loader loader, String internalName, Preferences preferences) throws TransformationException, IOException {

        // Init printer
        PlainTextPrinter printer = new PlainTextPrinter();
        return printer.buildDecompiledOutput(new JDLoader(loader), internalName, preferences, DECOMPILER);
    }


    @Override
    public Preferences defaultSettings() {
        return new Preferences();
    }

}
