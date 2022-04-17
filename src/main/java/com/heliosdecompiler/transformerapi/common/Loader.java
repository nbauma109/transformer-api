/*******************************************************************************
 * Copyright (C) 2022 GPLv3
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package com.heliosdecompiler.transformerapi.common;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import name.falgout.jeffrey.throwing.ThrowingFunction;

/**
 * A loader which is agnostic of the decompiler implementation
 */
public class Loader {

    private static final Map<String, ZipFile> openedZipFiles = new ConcurrentHashMap<>();
    
    private final Predicate<String> canLoadFunction;
    private final ThrowingFunction<String, byte[], IOException> loadFunction;
    private String[] classpathEntries;

    public Loader(Predicate<String> canLoadFunction, ThrowingFunction<String, byte[], IOException> loadFunction, URI jarURI) {
        this.canLoadFunction = canLoadFunction;
        this.loadFunction = loadFunction;
        if (jarURI != null) {
            List<String> jdkClasspath = ClasspathUtil.getJDKClasspath();
            classpathEntries = ClasspathUtil.createClasspathEntries(jarURI, jdkClasspath);
        }
    }

    public Loader(Predicate<String> predicate, ThrowingFunction<String, byte[], IOException> throwingFunction) {
        this(predicate, throwingFunction, null);
    }

    @SuppressWarnings("resource")
    public boolean canLoad(String internalName) {
        if (canLoadFunction.test(internalName)) {
            return true;
        }
        if (classpathEntries != null) {
            for (String classpathEntry : classpathEntries) {
                String extension = FilenameUtils.getExtension(classpathEntry);
                if ("jar".equals(extension) || "jmod".equals(extension)) {
                    try {
                        ZipFile zipFile = Maps.computeIfAbsent(openedZipFiles, classpathEntry, Loader::openZipFile);
                        ZipEntry zipEntry = getZipEntry(internalName, classpathEntry, zipFile);
                        if (zipEntry != null) {
                            return true;
                        }
                    } catch (IOException e) {
                        System.err.println(e);
                        return false;
                    }
                }
            }
        }
        return false;
    }


    public byte[] load(String internalName) throws IOException {
        byte[] classContents = loadFunction.apply(internalName);
        if (classContents != null) {
            return classContents;
        }
        if (classpathEntries != null) {
            for (String classpathEntry : classpathEntries) {
                String extension = FilenameUtils.getExtension(classpathEntry);
                if ("jar".equals(extension) || "jmod".equals(extension)) {
                    try {
                        @SuppressWarnings("resource")
                        ZipFile zipFile = Maps.computeIfAbsent(openedZipFiles, classpathEntry, Loader::openZipFile);
                        ZipEntry zipEntry = getZipEntry(internalName, classpathEntry, zipFile);
                        if (zipEntry != null) {
                            try (InputStream in = zipFile.getInputStream(zipEntry)) {
                                return IOUtils.toByteArray(in);
                            }
                        }
                    } catch (IOException e) {
                        System.err.println(e);
                    }
                }
            }
        }
        return null;
    }

    private static ZipFile openZipFile(String classpathEntry) throws IOException {
        return new ZipFile(new File(classpathEntry));
    }

    private static ZipEntry getZipEntry(String internalName, String classpathEntry, ZipFile zipFile) {
        StringBuilder entryName = new StringBuilder();
        if (classpathEntry.endsWith(".jmod")) {
            entryName.append("classes/");
        }
        entryName.append(internalName);
        if (!internalName.endsWith(".class")) {
            entryName.append(".class");
        }
        return zipFile.getEntry(entryName.toString());
    }
}
