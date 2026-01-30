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

import org.jd.core.v1.service.converter.classfiletojavasyntax.util.ExceptionUtil;
import org.jd.core.v1.util.ZipLoader;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InnerClassNode;
import org.vineflower.java.decompiler.main.extern.IContextSource;
import org.vineflower.java.decompiler.main.extern.IResultSaver;

import com.heliosdecompiler.transformerapi.common.FileLoader;
import com.heliosdecompiler.transformerapi.common.Loader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

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
     * @throws IOException
     */
    DecompilationResult decompile(Loader loader, String internalName, S settings) throws IOException;

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

    default DecompilationResult decompileFromArchive(String archivePath, String pkg, String className) throws IOException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Path path = Paths.get(archivePath);
        try (InputStream in = Files.newInputStream(path)) {
            ZipLoader zipLoader = new ZipLoader(in);
            Loader loader = new Loader(zipLoader::canLoad, zipLoader::load, path.toUri());
            String internalName = pkg + "/" + className.replaceFirst("\\.class$", "");
            return decompile(loader, internalName, lineNumberSettings());
        }
    }

    default DecompilationResult decompile(String rootLocation, String pkg, String className) throws IOException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        FileLoader fileLoader = new FileLoader(rootLocation, pkg, className);
        Loader loader = new Loader(fileLoader::canLoad, fileLoader::load, Paths.get(rootLocation).toUri());
        String internalName = pkg + "/" + className.replaceFirst("\\.class$", "");
        return decompile(loader, internalName, lineNumberSettings());
    }

    S defaultSettings() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException;

    S lineNumberSettings() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException;

    default Attributes getAllManifestAttributes() {
        Attributes allAttributes = new Attributes();
        try {
            Enumeration<URL> enumeration = getClass().getClassLoader().getResources("META-INF/MANIFEST.MF");

            while (enumeration.hasMoreElements()) {
                try (InputStream is = enumeration.nextElement().openStream()) {
                    Attributes attributes = new Manifest(is).getMainAttributes();
                    if (attributes != null) {
                        allAttributes.putAll(attributes);
                    }
                }
            }
        } catch (IOException e) {
            assert ExceptionUtil.printStackTrace(e);
        }
        return allAttributes;
    }

    long getDecompilationTime();

    String getDecompilerVersion();

    String getName();

    abstract class AbstractDecompiler {
        protected long time;
        private final String name;

        protected AbstractDecompiler(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
