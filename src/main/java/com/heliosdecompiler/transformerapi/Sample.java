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
package com.heliosdecompiler.transformerapi;

import com.heliosdecompiler.transformerapi.common.Loader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import jd.core.DecompilationResult;

public class Sample {

    public byte[] load(String internalName) throws IOException {
        InputStream is = this.getClass().getResourceAsStream("/" + internalName + ".class");

        if (is == null) {
            return null;
        }
        try (InputStream in=is; ByteArrayOutputStream out=new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int read = in.read(buffer);

            while (read > 0) {
                out.write(buffer, 0, read);
                read = in.read(buffer);
            }

            return out.toByteArray();
        }
    }

    public boolean canLoad(String internalName) {
        return this.getClass().getResource("/" + internalName + ".class") != null;
    }

    
    public static void main(String[] args) {
        Sample sample = new Sample();
        Loader loader = new Loader(sample::canLoad, sample::load);
        Map<String, String> preferences = new HashMap<>();
        try {
            String ff = StandardTransformers.Decompilers.ENGINE_VINEFLOWER;
            DecompilationResult result = StandardTransformers.decompile(loader, "java/lang/String", preferences, ff);
            System.out.println(result.getDecompiledOutput());
        } catch (Exception e) {
            System.err.println(e);
        }
    }
}
