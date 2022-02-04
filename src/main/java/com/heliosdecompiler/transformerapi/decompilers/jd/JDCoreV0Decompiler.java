package com.heliosdecompiler.transformerapi.decompilers.jd;

import com.heliosdecompiler.transformerapi.TransformationException;
import com.heliosdecompiler.transformerapi.common.Loader;
import com.heliosdecompiler.transformerapi.decompilers.Decompiler;

import java.io.IOException;

import jd.core.preferences.Preferences;
import jd.core.printer.PlainTextPrinter;
import jd.core.process.DecompilerImpl;

public class JDCoreV0Decompiler implements Decompiler<Preferences> {

    private static final DecompilerImpl DECOMPILER = new DecompilerImpl();

    @Override
    public String decompile(Loader loader, String internalName, Preferences preferences) throws TransformationException, IOException {

        // Init printer
        PlainTextPrinter printer = new PlainTextPrinter();
        return printer.buildDecompiledOutput(new JDLoader(loader), internalName, preferences, DECOMPILER);
    }


    @Override
    public Preferences defaultSettings() {
        return new Preferences();
    }

}
