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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import jadx.api.plugins.JadxPluginInfo;
import jadx.api.plugins.input.JadxInputPlugin;
import jadx.api.plugins.input.data.ILoadResult;
import jadx.api.plugins.input.data.impl.EmptyLoadResult;
import jadx.plugins.input.java.JavaClassReader;
import jadx.plugins.input.java.JavaLoadResult;

public class JavaInputPlugin implements JadxInputPlugin {

    public static final JadxPluginInfo PLUGIN_INFO = new JadxPluginInfo(
            "java-mem-input",
            "JavaMemInput",
            "Load .class fom memory");

    @Override
    public JadxPluginInfo getPluginInfo() {
        return PLUGIN_INFO;
    }

    @Override
    public ILoadResult loadFiles(List<Path> inputs) {
        List<JavaClassReader> readers = loadReaders(inputs);
        if (readers.isEmpty()) {
            return EmptyLoadResult.INSTANCE;
        }
        return new JavaLoadResult(readers, null);
    }

    private static List<JavaClassReader> loadReaders(List<Path> inputs) {
        List<JavaClassReader> readers = new ArrayList<>();
        for (int i = 0; i < inputs.size(); i++) {
            JADXInputPath jadxInputPath = (JADXInputPath) inputs.get(i);
            readers.add(new JavaClassReader(i, jadxInputPath.internalName(), jadxInputPath.data()));
        }
        return readers;
    }

}
