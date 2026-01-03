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

import java.util.HashMap;
import java.util.Map;
import java.util.jar.Manifest;

import jd.core.DecompilationResult;

public abstract class AbstractResultSaver {

    private static final String UNEXPECTED = "Unexpected";

    private final DecompilationResult result;
    private boolean lineNumbers;

    private final Map<String, String> results = new HashMap<>();

    protected AbstractResultSaver(DecompilationResult result) {
        this.result = result;
    }

    protected AbstractResultSaver() {
        this(new DecompilationResult());
    }

    public Map<String, String> getResults() {
        return this.results;
    }

    public boolean hasLineRemapping() {
        return lineNumbers;
    }

    public void saveClassEntry(String path, String archiveName, String qualifiedName, String entryName, String content) {
        throw new IllegalArgumentException(UNEXPECTED);
    }

    public void saveFolder(String path) {
    }

    public void copyFile(String source, String path, String entryName) {
    }

    public void saveClassFile(String path, String qualifiedName, String entryName, String content, int[] mapping) {
        if (mapping != null) {
            lineNumbers = true;
            for (int i = 0; i < mapping.length; i += 2) {
                int line = mapping[i + 1];
                int actualLine = mapping[i];
                result.putLineNumber(line, actualLine);
            }
        }
        results.put(qualifiedName, content);
    }

    public void createArchive(String path, String archiveName, Manifest manifest) {
        // nothing to do
    }

    public void saveDirEntry(String path, String archiveName, String entryName) {
        throw new IllegalArgumentException(UNEXPECTED);
    }

    public void copyEntry(String source, String path, String archiveName, String entry) {
        throw new IllegalArgumentException(UNEXPECTED);
    }

    public void closeArchive(String path, String archiveName) {
        // nothing to do
    }

}
