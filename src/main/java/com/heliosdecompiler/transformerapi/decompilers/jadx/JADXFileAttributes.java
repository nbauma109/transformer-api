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