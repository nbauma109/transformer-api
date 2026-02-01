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
import org.apache.commons.lang3.function.FailableFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vineflower.java.decompiler.main.extern.IContextSource;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * A loader which is agnostic of the decompiler implementation
 */
public class Loader implements IContextSource {

    private static final Map<String, ZipFile> openedZipFiles = new ConcurrentHashMap<>();
    private static final Logger log = LoggerFactory.getLogger(Loader.class);

    private final Predicate<String> canLoadFunction;
    private final FailableFunction<String, byte[], IOException> loadFunction;
    private String[] classpathEntries;

    public Loader(Predicate<String> canLoadFunction, FailableFunction<String, byte[], IOException> loadFunction, URI jarURI) {
        this.canLoadFunction = canLoadFunction;
        this.loadFunction = loadFunction;
        if (jarURI != null) {
            List<String> jdkClasspath = ClasspathUtil.getJDKClasspath();
            classpathEntries = ClasspathUtil.createClasspathEntries(jarURI, jdkClasspath);
        }
    }

    public Loader(Predicate<String> predicate, FailableFunction<String, byte[], IOException> throwingFunction) {
        this(predicate, throwingFunction, null);
    }

    private void loadClasspathEntries() {
        for (String classpathEntry : classpathEntries) {
            String extension = FilenameUtils.getExtension(classpathEntry);
            if ("jar".equals(extension) || "jmod".equals(extension)) {
                try {
                    //noinspection resource
                    Maps.computeIfAbsent(openedZipFiles, classpathEntry, Loader::openZipFile);
                } catch (IOException e) {
                    log.error("e: ", e);
                }
            }
        }
    }

    public boolean canLoad(String internalName) {
        if (canLoadFunction.test(internalName)) {
            return true;
        }
        if (classpathEntries != null) {
            loadClasspathEntries();
            for (Map.Entry<String, ZipFile> entry : openedZipFiles.entrySet()) {
                ZipFile zipFile = entry.getValue();
                ZipEntry zipEntry = getZipEntry(internalName, entry.getKey(), zipFile);
                if (zipEntry != null) {
                    return true;
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
            loadClasspathEntries();
            for (Map.Entry<String, ZipFile> entry : openedZipFiles.entrySet()) {
                ZipFile zipFile = entry.getValue();
                ZipEntry zipEntry = getZipEntry(internalName, entry.getKey(), zipFile);
                if (zipEntry != null) {
                    try (InputStream in = zipFile.getInputStream(zipEntry)) {
                        return IOUtils.toByteArray(in);
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
        if (!internalName.endsWith(CLASS_SUFFIX)) {
            entryName.append(CLASS_SUFFIX);
        }
        return zipFile.getEntry(entryName.toString());
    }

    @Override
    public String getName() {
        return "TransformerApi Loader";
    }

    @Override
    public Entries getEntries() {
        if (classpathEntries == null) {
            return Entries.EMPTY;
        }
        loadClasspathEntries();
        List<Entry> classes = new ArrayList<>();
        for (Map.Entry<String, ZipFile> entry : openedZipFiles.entrySet()) {
            ZipFile zipFile = entry.getValue();
            zipFile.stream().forEach(zipEntry -> {
                String entryName = zipEntry.getName();
                if (entryName.endsWith(CLASS_SUFFIX)) {
                    String internalName = entryName.substring(0, entryName.length() - 6);
                    classes.add(Entry.parse(internalName));
                }
            });
        }

        return new Entries(
                classes,
                List.of(),
                List.of()
        );
    }

    @Override
    public InputStream getInputStream(String resource) throws IOException {
        return new ByteArrayInputStream(load(resource.replaceFirst("\\.class$", "")));
    }
}
