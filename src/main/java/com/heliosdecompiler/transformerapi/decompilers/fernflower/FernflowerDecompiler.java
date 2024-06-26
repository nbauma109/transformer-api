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

import java.io.File;
import java.io.IOException;
import java.util.Map;

import jd.core.DecompilationResult;

/**
 * Provides a gateway to the Fernflower decompiler
 */
public class FernflowerDecompiler implements Decompiler<FernflowerSettings> {

    @Override
    public DecompilationResult decompile(Loader loader, String internalName, FernflowerSettings settings) throws TransformationException, IOException {
        ClassStruct classStruct = readClassAndInnerClasses(loader, internalName);
        if (!classStruct.importantData().isEmpty()) {
            IBytecodeProvider provider = new FernflowerBytecodeProvider(classStruct.importantData());
            FernflowerResultSaver saver = new FernflowerResultSaver();
            Fernflower baseDecompiler = new Fernflower(provider, saver, settings.getSettings(), new PrintStreamLogger(System.out));
            StructContext context;
            try {
                context = DecompilerContext.getStructContext();
                for (Map.Entry<String, byte[]> ent : classStruct.importantData().entrySet()) {
                    context.addSpace(new File(ent.getKey() + ".class"), true);
                }
                baseDecompiler.decompileContext();
            } catch (Exception t) {
                DecompilerContext.getLogger().writeMessage("Error while decompiling", t);
            } finally {
                baseDecompiler.clearContext();
            }
            DecompilationResult decompilationResult = new DecompilationResult();
            String key = classStruct.fullClassName();
            decompilationResult.setDecompiledOutput(saver.getResults().get(key));
            return decompilationResult;
        }
        return null;
    }

    @Override
    public FernflowerSettings defaultSettings() {
        return new FernflowerSettings();
    }
}
