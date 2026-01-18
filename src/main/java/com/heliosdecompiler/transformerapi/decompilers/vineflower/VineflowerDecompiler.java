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

package com.heliosdecompiler.transformerapi.decompilers.vineflower;

import org.apache.commons.lang3.time.StopWatch;
import org.vineflower.java.decompiler.main.DecompilerContext;
import org.vineflower.java.decompiler.main.Fernflower;
import org.vineflower.java.decompiler.main.decompiler.PrintStreamLogger;
import org.vineflower.java.decompiler.main.extern.IFernflowerPreferences;
import org.vineflower.java.decompiler.main.extern.TextTokenVisitor;

import com.heliosdecompiler.transformerapi.common.Loader;
import com.heliosdecompiler.transformerapi.decompilers.Decompiler;

import java.io.IOException;

import jd.core.DecompilationResult;

/**
 * Provides a gateway to the Fernflower decompiler
 */
public class VineflowerDecompiler implements Decompiler<VineflowerSettings> {

    private long time;

    @Override
    public DecompilationResult decompile(Loader loader, String internalName, VineflowerSettings settings) throws IOException {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        DecompilationResult decompilationResult = new DecompilationResult();
        ClassStruct classStruct = readClassAndInnerClasses(loader, internalName);
        if (!classStruct.importantData().isEmpty()) {
            VineflowerResultSaver saver = new VineflowerResultSaver(decompilationResult);
            Fernflower baseDecompiler = new Fernflower(saver, settings.getSettings(), new PrintStreamLogger(System.out));
            if (!"1".equals(settings.getSettings().getOrDefault(IFernflowerPreferences.DUMP_ORIGINAL_LINES, "0"))) {
                TextTokenVisitor.addVisitor(next -> new VineflowerTokenConsumer(decompilationResult, next));
            }
            baseDecompiler.addSource(classStruct);
            baseDecompiler.addLibrary(loader);
            try {
                baseDecompiler.decompileContext();
            } catch (Exception t) {
                DecompilerContext.getLogger().writeMessage("Error while decompiling", t);
            } finally {
                baseDecompiler.clearContext();
            }
            String key = classStruct.fullClassName();
            String decompiledOutput = saver.getResults().get(key);
            decompilationResult.setDecompiledOutput(decompiledOutput);
            if (!saver.hasLineRemapping()) {
                int lineCount = decompiledOutput.split("\n").length;
                decompilationResult.setMaxLineNumber(lineCount);
                for (int i = 1; i <= lineCount; i++) {
                    decompilationResult.putLineNumber(i, i);
                }
            }
        }
        time = stopWatch.getTime();
        return decompilationResult;
    }

    @Override
    public long getDecompilationTime() {
        return time;
    }

    @Override
    public String getDecompilerVersion() {
        return getAllManifestAttributes().getValue("vineflower-version");
    }

    @Override
    public VineflowerSettings defaultSettings() {
        return new VineflowerSettings();
    }

    @Override
    public VineflowerSettings lineNumberSettings() {
        return new VineflowerSettings(VineflowerSettings.lineNumbers());
    }
}
