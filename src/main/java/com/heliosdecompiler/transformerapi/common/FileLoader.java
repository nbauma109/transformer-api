/*
 * Copyright 2026 Apache License, Version 2.0
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

package com.heliosdecompiler.transformerapi.common;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.jd.core.v1.api.loader.Loader;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Objects;
import java.util.regex.Pattern;


public class FileLoader implements Loader {

    protected static final Pattern CLASS_SUFFIX_PATTERN = Pattern.compile("\\.class$");

    private final HashMap<String, byte[]> map = new HashMap<>();

    public FileLoader(String rootLocation, String pkg, String className) throws IOException {
        Objects.requireNonNull(rootLocation, "rootLocation");
        Objects.requireNonNull(className, "className");

        Path rootDirectory = Paths.get(rootLocation);
        Validate.isTrue(Files.isDirectory(rootDirectory), "Not a directory: " + rootDirectory);

        String topLevelTypeName = stripClassSuffix(className);
        String primaryInternalName = buildInternalName(pkg, topLevelTypeName);

        Path classDirectory = pkg.isEmpty() ? rootDirectory : rootDirectory.resolve(pkg);
        Path primaryClassFile = classDirectory.resolve(toClassFileName(topLevelTypeName));

        Validate.isTrue(Files.isRegularFile(primaryClassFile), "Class file not found: " + primaryClassFile);

        map.put(primaryInternalName, Files.readAllBytes(primaryClassFile));
        loadInnerClasses(classDirectory, pkg, topLevelTypeName);
    }

    protected void loadInnerClasses(Path classDirectory, String packagePath, String topLevelTypeName) throws IOException {
        String pattern = topLevelTypeName + "$*.class";

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(classDirectory, pattern)) {
            for (Path innerClassFile : stream) {
                if (Files.isRegularFile(innerClassFile)) {
                    String fileName = String.valueOf(innerClassFile.getFileName());
                    String innerTypeName = makeEntryName(fileName);
                    String internalName = buildInternalName(packagePath, innerTypeName);
                    map.put(internalName, Files.readAllBytes(innerClassFile));
                }
            }
        }
    }

    protected String buildInternalName(String packagePath, String typeName) {
        Objects.requireNonNull(typeName, "typeName");
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotEmpty(packagePath)) {
            sb.append(packagePath);
            sb.append("/");
        }
        sb.append(typeName);
        return sb.toString();
    }

    protected String stripClassSuffix(String name) {
        return CLASS_SUFFIX_PATTERN.matcher(name).replaceFirst("");
    }

    protected String toClassFileName(String typeName) {
        return stripClassSuffix(typeName) + ".class";
    }

    protected String makeEntryName(String entryName) {
        return CLASS_SUFFIX_PATTERN.matcher(entryName).replaceFirst("");
    }

    @Override
    public byte[] load(String internalName) throws IOException {
        return map.get(stripClassSuffix(internalName));
    }

    @Override
    public boolean canLoad(String internalName) {
        return map.containsKey(stripClassSuffix(internalName));
    }
}