package com.heliosdecompiler.transformerapi;

import org.jd.core.v1.util.StringConstants;

import java.io.IOException;
import java.io.InputStream;

public class ZipLoader extends org.jd.core.v1.util.ZipLoader {

    public ZipLoader(InputStream in) throws IOException {
        super(in);
    }

    @Override
    protected String makeEntryName(String entryName) {
        return entryName;
    }

    @Override
    public byte[] load(String internalName) throws IOException {
        if (internalName.endsWith(StringConstants.CLASS_FILE_SUFFIX)) {
            return super.load(internalName);
        }
        return getMap().get(internalName + StringConstants.CLASS_FILE_SUFFIX);
    }

    @Override
    public boolean canLoad(String internalName) {
        if (internalName.endsWith(StringConstants.CLASS_FILE_SUFFIX)) {
            return super.canLoad(internalName);
        }
        return getMap().containsKey(internalName + StringConstants.CLASS_FILE_SUFFIX);
    }
}