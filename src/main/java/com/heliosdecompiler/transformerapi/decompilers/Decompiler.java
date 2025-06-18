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
import org.vineflower.java.decompiler.main.extern.IContextSource;
import org.vineflower.java.decompiler.main.extern.IResultSaver;

import com.heliosdecompiler.transformerapi.TransformationException;
import com.heliosdecompiler.transformerapi.common.Loader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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

    default ClassStruct readClassAndInnerClasses(Loader loader, String internalName) throws IOException {
        return readClassAndInnerClasses(new HashMap<>(), loader, internalName);
    }

    default ClassStruct readClassAndInnerClasses(Map<String, byte[]> importantData, Loader loader, String internalName) throws IOException {
        String fullClassName = internalName.replace('/', '.');
        if (!importantData.containsKey(internalName) && loader.canLoad(internalName)) {
            byte[] data = loader.load(internalName);
            importantData.put(internalName, data);
            ClassReader reader = new ClassReader(data);
            ClassNode classNode = new ClassNode();
            reader.accept(classNode, ClassReader.SKIP_FRAMES | ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG);
            fullClassName = classNode.name;
            if (classNode.innerClasses != null) {
                for (InnerClassNode icn : classNode.innerClasses) {
                    if (icn.name.startsWith(internalName + '$')) {
                        importantData.putAll(readClassAndInnerClasses(importantData, loader, icn.name).importantData);
                    }
                }
            }
        }
        return new ClassStruct(fullClassName, importantData);
    }

    record ClassStruct(String fullClassName, Map<String, byte[]> importantData) implements IContextSource {

        @Override
        public String getName() {
            return "TransformerAPI ClassStruct";
        }

        @Override
        public Entries getEntries() {
            List<Entry> classes = new ArrayList<>();
            for (String key : importantData.keySet()) {
                classes.add(Entry.parse(key));
            }
            return new Entries(classes, Collections.emptyList(), Collections.emptyList());
        }

        @Override
        public InputStream getInputStream(String resource) throws IOException {
            return new ByteArrayInputStream(importantData.get(resource.replaceFirst("\\.class$", "")));
        }

        @Override
        public IOutputSink createOutputSink(IResultSaver saver) {
            return new IOutputSink() {
                @Override
                public void close() throws IOException {
                }

                @Override
                public void begin() {
                }

                @Override
                public void acceptOther(String path) {
                 // not used
                }

                @Override
                public void acceptDirectory(String directory) {
                    // not used
                }

                @Override
                public void acceptClass(String qualifiedName, String fileName, String content, int[] mapping) {
                    saver.saveClassFile("", qualifiedName, fileName, content, mapping);
                }
            };
        }
    }
}
