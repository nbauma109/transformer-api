/*
 * © 2026 Nicolas Baumann (@nbauma109)
 *
 * Licensed under the Apache License, Version 2.0.
 */

package com.heliosdecompiler.transformerapi.common;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Regression coverage for on-demand class loading from a compiled test output directory.
 */
public class FileLoaderTest {

    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testLoadReadsSiblingSuperclassOnDemand() throws Exception {
        FileLoader loader = new FileLoader("target/test-classes", "com/heliosdecompiler/transformerapi", "TestLinkCoverage.class");

        byte[] data = loader.load("com/heliosdecompiler/transformerapi/TestLinkCoverageBase");

        assertTrue(data.length > 0);
    }

    @Test
    public void testLoadReturnsEmptyArrayForMissingType() throws Exception {
        FileLoader loader = new FileLoader("target/test-classes", "com/heliosdecompiler/transformerapi", "TestLinkCoverage.class");

        assertNull(loader.load("com/heliosdecompiler/transformerapi/DoesNotExist"));
    }

    @Test
    public void testLoadAcceptsNullAndEmptyPackage() throws IOException {
        Path root = temporaryFolder.getRoot().toPath();
        byte[] classBytes = Files.readAllBytes(Path.of("target/test-classes",
                "com/heliosdecompiler/transformerapi/TestLinkCoverage.class"));
        Files.write(root.resolve("TestLinkCoverage.class"), classBytes);

        FileLoader nullPackageLoader = new FileLoader(root.toString(), null, "TestLinkCoverage.class");
        FileLoader emptyPackageLoader = new FileLoader(root.toString(), "", "TestLinkCoverage");

        assertArrayEquals(classBytes, nullPackageLoader.load("TestLinkCoverage"));
        assertArrayEquals(classBytes, emptyPackageLoader.load("TestLinkCoverage.class"));
    }
}
