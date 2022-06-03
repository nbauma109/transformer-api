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

package com.heliosdecompiler.transformerapi.decompilers.fernflower;

import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.main.Fernflower;
import org.jetbrains.java.decompiler.main.decompiler.PrintStreamLogger;
import org.jetbrains.java.decompiler.main.extern.IBytecodeProvider;
import org.jetbrains.java.decompiler.struct.StructContext;

import com.heliosdecompiler.transformerapi.TransformationException;
import com.heliosdecompiler.transformerapi.common.Loader;
import com.heliosdecompiler.transformerapi.decompilers.Decompiler;

import java.io.IOException;
import java.util.Map;

import jd.core.DecompilationResult;

/**
 * Provides a gateway to the Fernflower decompiler
 */
public class FernflowerDecompiler implements Decompiler<FernflowerSettings> {

    @Override
    public DecompilationResult decompile(Loader loader, String internalName, FernflowerSettings settings) throws TransformationException, IOException {
        Map<String, byte[]> importantData = readClassAndInnerClasses(loader, internalName);
        if (!importantData.isEmpty()) {
            IBytecodeProvider provider = new FernflowerBytecodeProvider(importantData);
            FernflowerResultSaver saver = new FernflowerResultSaver();
            Fernflower baseDecompiler = new Fernflower(provider, saver, settings.getSettings(), new PrintStreamLogger(System.out));
            
            try {
                StructContext context = DecompilerContext.getStructContext();

                for (Map.Entry<String, byte[]> ent : importantData.entrySet()) {
                    try {
                        context.addData("", ent.getKey() + ".class", ent.getValue(), true); // Fernflower will .substring(".class") to replace the extension
                    } catch (Exception e) {
                        DecompilerContext.getLogger().writeMessage("Corrupted class file: " + ent.getKey(), e);
                    }
                }

                baseDecompiler.decompileContext();
            } catch (Exception t) {
                DecompilerContext.getLogger().writeMessage("Error while decompiling", t);
            } finally {
                baseDecompiler.clearContext();
            }
            DecompilationResult decompilationResult = new DecompilationResult();
            decompilationResult.setDecompiledOutput(saver.getResults().get(internalName));
            return decompilationResult;
        }
        return null;
    }

    @Override
    public FernflowerSettings defaultSettings() {
        return new FernflowerSettings();
    }
}
