package com.heliosdecompiler.transformerapi.decompilers.cfr;

import org.benf.cfr.reader.api.ClassFileSource;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.Pair;

import com.heliosdecompiler.transformerapi.common.Loader;

import java.io.IOException;
import java.util.Collection;

public class CFRDataSource implements ClassFileSource {
    private Loader loader;
    private byte[] data;
    private String name;

    public CFRDataSource(Loader loader, byte[] data, String name) {
        this.loader = loader;
        this.data = data;
        this.name = name;
    }

    @Override
    public void informAnalysisRelativePathDetail(String usePath, String classFilePath) {
    }

    @Override
    public Collection<String> addJar(String jarPath) {
        throw new UnsupportedOperationException();
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
        if (!s.endsWith(".class")) {
            throw new IllegalArgumentException("Not a .class file");
        }
        String internalName = s.substring(0, s.length() - 6);
        return Pair.make(loader.load(internalName), internalName);
    }
}