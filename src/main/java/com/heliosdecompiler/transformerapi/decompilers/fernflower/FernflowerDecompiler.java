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
import org.jetbrains.java.decompiler.struct.ContextUnit;
import org.jetbrains.java.decompiler.struct.StructClass;
import org.jetbrains.java.decompiler.struct.StructContext;
import org.jetbrains.java.decompiler.struct.lazy.LazyLoader;
import org.jetbrains.java.decompiler.util.DataInputFullStream;

import com.heliosdecompiler.transformerapi.TransformationException;
import com.heliosdecompiler.transformerapi.common.Loader;
import com.heliosdecompiler.transformerapi.decompilers.Decompiler;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;

import jd.core.DecompilationResult;

/**
 * Provides a gateway to the Fernflower decompiler
 */
public class FernflowerDecompiler implements Decompiler<FernflowerSettings> {
    private static Field UNITS_FIELD;
    private static Field LOADER_FIELD;
    private static Field STRUCT_CONTEXT_FIELD;

    static {
        try {
            UNITS_FIELD = StructContext.class.getDeclaredField("units");
            UNITS_FIELD.setAccessible(true);
            LOADER_FIELD = StructContext.class.getDeclaredField("loader");
            LOADER_FIELD.setAccessible(true);
            STRUCT_CONTEXT_FIELD = Fernflower.class.getDeclaredField("structContext");
            STRUCT_CONTEXT_FIELD.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("Could not initialize Fernflower decompiler", e);
        }
    }

    @Override
    public DecompilationResult decompile(Loader loader, String internalName, FernflowerSettings settings) throws TransformationException, IOException {
        ClassStruct classStruct = readClassAndInnerClasses(loader, internalName);
        if (!classStruct.importantData().isEmpty()) {
            IBytecodeProvider provider = new FernflowerBytecodeProvider(classStruct.importantData());
            FernflowerResultSaver saver = new FernflowerResultSaver();
            Fernflower baseDecompiler = new Fernflower(provider, saver, settings.getSettings(), new PrintStreamLogger(System.out));

            try {
                StructContext context = (StructContext) STRUCT_CONTEXT_FIELD.get(baseDecompiler);
                Map<String, ContextUnit> units = (Map<String, ContextUnit>) UNITS_FIELD.get(context);
                LazyLoader lazyLoader = (LazyLoader) LOADER_FIELD.get(context);

                ContextUnit defaultUnit = units.get("");

                for (Map.Entry<String, byte[]> ent : classStruct.importantData().entrySet()) {
                    try {
                        @SuppressWarnings("resource") // because close() has no effect on ByteArrayInputStream
                        StructClass structClass = StructClass.create(new DataInputFullStream(ent.getValue()), true, lazyLoader);
                        context.getClasses().put(structClass.qualifiedName, structClass);
                        defaultUnit.addClass(structClass, ent.getKey() + ".class"); // Fernflower will .substring(".class") to replace the extension
                        lazyLoader.addClassLink(structClass.qualifiedName, new LazyLoader.Link(ent.getKey(), (String) null));
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
