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
package com.heliosdecompiler.transformerapi.decompilers.jadx;

import org.apache.commons.lang3.time.StopWatch;

import com.heliosdecompiler.transformerapi.common.Loader;
import com.heliosdecompiler.transformerapi.decompilers.Decompiler;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jadx.api.JadxArgs;
import jadx.api.JadxDecompiler;
import jadx.api.JavaClass;
import jadx.core.Jadx;
import jadx.plugins.input.java.JavaClassReader;
import jadx.plugins.input.java.JavaLoadResult;
import jd.core.DecompilationResult;

public class JADXDecompiler extends Decompiler.AbstractDecompiler implements Decompiler<JadxArgs> {

    public JADXDecompiler(String name) {
        super(name);
    }

    @Override
    public DecompilationResult decompile(Loader loader, String internalName, JadxArgs args) throws IOException {
        StopWatch stopWatch = StopWatch.createStarted();

        DecompilationResult decompilationResult = new DecompilationResult();
        Map<String, byte[]> importantData = readClassAndInnerClasses(loader, internalName).importantData();
        if (!importantData.isEmpty()) {
            int i = 0;
            List<JavaClassReader> readers = new ArrayList<>();
            for (Map.Entry<String, byte[]> ent : importantData.entrySet()) {
                readers.add(new JavaClassReader(i++, ent.getKey(), ent.getValue()));
            }
            try (JadxDecompiler jadx = new JadxDecompiler(args); JavaLoadResult javaLoadResult = new JavaLoadResult(readers, null)) {
                jadx.addCustomCodeLoader(javaLoadResult);
                jadx.load();
                for (JavaClass cls : jadx.getClasses()) {
                    if (cls.getClassNode().getClsData().getInputFileName().equals(internalName)) {
                        decompilationResult.setDecompiledOutput(cls.getCode());
                        break;
                    }
                }
            }
        }

        time = stopWatch.getTime();

        return decompilationResult;
    }

    @Override
    public long getDecompilationTime() {
        return time;
    }

    @Override
    public String getDecompilerVersion() {
        return Jadx.getVersion();
    }

    @Override
    public JadxArgs defaultSettings() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        return new MapJadxArgs(new HashMap<>());
    }

    @Override
    public JadxArgs lineNumberSettings() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        return new MapJadxArgs(MapJadxArgs.lineNumbers());
    }
}
