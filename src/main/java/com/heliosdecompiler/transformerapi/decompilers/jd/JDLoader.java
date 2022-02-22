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

import com.heliosdecompiler.transformerapi.common.Loader;

import java.io.IOException;

public final class JDLoader implements org.jd.core.v1.api.loader.Loader {

    private final Loader loader;

    public JDLoader(Loader loader) {
        this.loader = loader;
    }

    @Override
    public byte[] load(String internalName) throws IOException {
        return loader.load(internalName);
    }

    @Override
    public boolean canLoad(String internalName) {
        return loader.canLoad(internalName);
    }
}