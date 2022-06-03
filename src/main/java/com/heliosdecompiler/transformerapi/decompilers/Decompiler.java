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

package com.heliosdecompiler.transformerapi.decompilers;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InnerClassNode;

import com.heliosdecompiler.transformerapi.TransformationException;
import com.heliosdecompiler.transformerapi.common.Loader;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import jd.core.DecompilationResult;

/**
 * Represents a particular Decompiler. Note that decompiler implementations
 * should be stateless, and thus can be reused (and are thread safe)
 */
public interface Decompiler<S> {

    /**
     * Decompile the given class with a loader
     * 
     * @param loader       The loader implementation used to load the class
     * @param internalName The internal name of the class to decompile
     * @param settings     The settings to use with this decompiler
     * @return The decompilation result
     * @throws TransformationException
     * @throws IOException
     */
    DecompilationResult decompile(Loader loader, String internalName, S settings) throws TransformationException, IOException;

    S defaultSettings();

    default DecompilationResult decompile(Loader loader, String internalName) throws TransformationException, IOException {
        return decompile(loader, internalName, defaultSettings());
    }

    default Map<String, byte[]> readClassAndInnerClasses(Loader loader, String internalName) throws IOException {
        Map<String, byte[]> importantData = new HashMap<>();
        return readClassAndInnerClasses(importantData, loader, internalName);
    }

    default Map<String, byte[]> readClassAndInnerClasses(Map<String, byte[]> importantData, Loader loader, String internalName) throws IOException {
        if (!importantData.containsKey(internalName) && loader.canLoad(internalName)) {
            byte[] data = loader.load(internalName);
            importantData.put(internalName, data);
            ClassReader reader = new ClassReader(data);
            ClassNode classNode = new ClassNode();
            reader.accept(classNode, ClassReader.SKIP_FRAMES | ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG);

            if (classNode.innerClasses != null) {
                for (InnerClassNode icn : classNode.innerClasses) {
                    if (icn.name.startsWith(internalName + '$')) {
                        importantData.putAll(readClassAndInnerClasses(importantData, loader, icn.name));
                    }
                }
            }
        }
        return importantData;
    }
}
