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
package com.heliosdecompiler.transformerapi.decompilers.cfr;

import org.benf.cfr.reader.bytecode.analysis.parse.utils.Pair;
import org.benf.cfr.reader.state.ClassFileSourceImpl;
import org.benf.cfr.reader.util.getopt.Options;

import com.heliosdecompiler.transformerapi.common.Loader;

import java.io.IOException;

import jd.core.ClassUtil;

public class CFRDataSource extends ClassFileSourceImpl {
    private Loader loader;
    private byte[] data;
    private String name;

    public CFRDataSource(Loader loader, byte[] data, String name, Options options) {
        super(options);
        this.loader = loader;
        this.data = data;
        this.name = name;
    }

    @Override
    public void informAnalysisRelativePathDetail(String usePath, String classFilePath) {
    }

    @Override
    public String getPossiblyRenamedPath(String s) {
        return s;
    }

    @Override
    public Pair<byte[], String> getClassFileContent(String s) throws IOException {
        if (s.equals(name)) {
            return Pair.make(data, name);
        }
        String internalName = ClassUtil.getInternalName(s);
        if (loader.canLoad(internalName)) {
            return Pair.make(loader.load(internalName), internalName);
        }
        return super.getClassFileContent(s);
    }
}