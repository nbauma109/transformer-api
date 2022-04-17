/*
 * Copyright 2017 Sam Sun <github-contact@samczsun.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.heliosdecompiler.transformerapi.decompilers.cfr;

import org.benf.cfr.reader.api.CfrDriver;
import org.benf.cfr.reader.util.getopt.OptionsImpl;

import com.heliosdecompiler.transformerapi.TransformationException;
import com.heliosdecompiler.transformerapi.common.Loader;
import com.heliosdecompiler.transformerapi.decompilers.Decompiler;

import java.io.IOException;
import java.util.Arrays;

import jd.core.DecompilationResult;

public class CFRDecompiler implements Decompiler<CFRSettings> {

    @Override
    public DecompilationResult decompile(Loader loader, String internalName, CFRSettings settings) throws TransformationException, IOException {
        CFROutputStreamFactory sink = new CFROutputStreamFactory();
        String entryPath = internalName + ".class";
        OptionsImpl options = new OptionsImpl(settings.getSettings());
        CfrDriver driver = new CfrDriver.Builder()
            .withBuiltOptions(options)
            .withClassFileSource(new CFRDataSource(loader, loader.load(internalName), entryPath, options))
            .withOutputSink(sink)
            .build();
        driver.analyse(Arrays.asList(entryPath));
        DecompilationResult decompilationResult = new DecompilationResult();
        decompilationResult.setDecompiledOutput(sink.getGeneratedSource());
        return decompilationResult;
    }

    @Override
    public CFRSettings defaultSettings() {
        return new CFRSettings();
    }
}
