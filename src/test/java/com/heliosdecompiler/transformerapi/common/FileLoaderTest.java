/*
 * © 2026 Nicolas Baumann (@nbauma109)
 *
 * Licensed under the Apache License, Version 2.0.
 */

package com.heliosdecompiler.transformerapi.common;

import org.junit.Test;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Regression coverage for on-demand class loading from a compiled test output directory.
 */
public class FileLoaderTest {

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
}
