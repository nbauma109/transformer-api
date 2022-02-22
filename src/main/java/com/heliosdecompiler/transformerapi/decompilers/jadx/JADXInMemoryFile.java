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

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;

public class JADXInMemoryFile extends File {

    private static final long serialVersionUID = 1L;

    private final String internalName;
    private final byte[] data;

    public JADXInMemoryFile(String internalName, byte[] data) {
        super(internalName);
        this.internalName = internalName;
        this.data = data;
    }

    public String getInternalName() {
        return internalName;
    }

    @Override
    public Path toPath() {
        return new JADXInputPath(internalName, data);
    }

    @Override
    public boolean exists() {
        return true;
    }

    @Override
    public String toString() {
        return internalName;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Arrays.hashCode(data);
        result = 31 * result + Objects.hash(internalName);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj) || getClass() != obj.getClass()) {
            return false;
        }
        JADXInMemoryFile other = (JADXInMemoryFile) obj;
        return Arrays.equals(data, other.data) && Objects.equals(internalName, other.internalName);
    }
}
