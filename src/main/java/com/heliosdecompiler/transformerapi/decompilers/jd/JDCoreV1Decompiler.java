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

import org.jd.core.v1.ClassFileToJavaSourceDecompiler;
import org.jd.core.v1.printer.ClassFilePrinter;
import org.jd.core.v1.util.StringConstants;

import com.heliosdecompiler.transformerapi.TransformationException;
import com.heliosdecompiler.transformerapi.common.Loader;
import com.heliosdecompiler.transformerapi.decompilers.Decompiler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import jd.core.DecompilationResult;

public class JDCoreV1Decompiler implements Decompiler<Map<String, String>> {

    public static final ClassFileToJavaSourceDecompiler DECOMPILER = new ClassFileToJavaSourceDecompiler();

    @Override
    public DecompilationResult decompile(Loader loader, String internalName, Map<String, String> preferences) throws TransformationException, IOException {

        ClassFilePrinter printer = new ClassFilePrinter();

        String decompiledOutput = printer.buildDecompiledOutput(preferences, new JDLoader(loader), internalName + StringConstants.CLASS_FILE_SUFFIX, DECOMPILER);
        printer.getResult().setDecompiledOutput(decompiledOutput);
        return printer.getResult();
    }

    @Override
    public Map<String, String> defaultSettings() {
        return new HashMap<>();
    }
}
