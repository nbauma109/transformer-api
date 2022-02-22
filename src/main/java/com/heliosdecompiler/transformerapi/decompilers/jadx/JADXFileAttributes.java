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

import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

public final class JADXFileAttributes implements BasicFileAttributes {

    @Override
    public long size() {
        throw new UnsupportedOperationException();
    }

    @Override
    public FileTime lastModifiedTime() {
        throw new UnsupportedOperationException();
    }

    @Override
    public FileTime lastAccessTime() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isSymbolicLink() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isRegularFile() {
        return true;
    }

    @Override
    public boolean isOther() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isDirectory() {
        return false;
    }

    @Override
    public Object fileKey() {
        throw new UnsupportedOperationException();
    }

    @Override
    public FileTime creationTime() {
        throw new UnsupportedOperationException();
    }
}