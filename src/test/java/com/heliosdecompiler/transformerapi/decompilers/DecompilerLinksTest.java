/*
 * Copyright 2026 Nicolas Baumann (@nbauma109)
 *
 * Licensed under the Apache License, Version 2.0.
 */

package com.heliosdecompiler.transformerapi.decompilers;

import com.heliosdecompiler.transformerapi.StandardTransformers.Decompilers;
import com.heliosdecompiler.transformerapi.common.Loader;
import com.heliosdecompiler.transformerapi.decompilers.cfr.CFRSettings;
import com.heliosdecompiler.transformerapi.decompilers.fernflower.FernflowerSettings;
import com.heliosdecompiler.transformerapi.decompilers.jadx.MapJadxArgs;

import org.apache.commons.io.IOUtils;
import org.jd.core.v1.util.ZipLoader;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import jd.core.DecompilationResult;
import jd.core.links.ReferenceData;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Regression coverage for hyperlink metadata produced by the supported decompilers.
 */
public class DecompilerLinksTest {

    @Test
    public void testFernflowerProvidesLinks() throws Exception {
        assertProvidesBasicLinks(Decompilers.FERNFLOWER.decompile(loader("/test-compact-expand-inline.jar"), "test/TestCompact", new FernflowerSettings(FernflowerSettings.defaults())));
    }

    @Test
    public void testCfrProvidesLinks() throws Exception {
        assertProvidesBasicLinks(Decompilers.CFR.decompile(loader("/test-compact-expand-inline.jar"), "test/TestCompact", new CFRSettings(CFRSettings.defaults())));
    }

    @Test
    public void testJadxProvidesLinks() throws Exception {
        assertProvidesBasicLinks(Decompilers.JADX.decompile(loader("/test-compact-expand-inline.jar"), "test/TestCompact", new MapJadxArgs(MapJadxArgs.defaults())));
    }

    @Test
    public void testFernflowerProvidesBeanLinks() throws Exception {
        assertProvidesBeanLinks(Decompilers.FERNFLOWER.decompile(loader("/test-bean.jar"), "com/heliosdecompiler/transformerapi/TestBean", new FernflowerSettings(FernflowerSettings.defaults())));
    }

    @Test
    public void testCfrProvidesBeanLinks() throws Exception {
        assertProvidesBeanLinks(Decompilers.CFR.decompile(loader("/test-bean.jar"), "com/heliosdecompiler/transformerapi/TestBean", new CFRSettings(CFRSettings.defaults())));
    }

    @Test
    public void testJadxProvidesBeanLinks() throws Exception {
        assertProvidesBeanLinks(Decompilers.JADX.decompile(loader("/test-bean.jar"), "com/heliosdecompiler/transformerapi/TestBean", new MapJadxArgs(MapJadxArgs.defaults())));
    }

    @Test
    public void testFernflowerProvidesCoverageLinks() throws Exception {
        DecompilationResult result = Decompilers.FERNFLOWER.decompile("target/test-classes", "com/heliosdecompiler/transformerapi", "TestLinkCoverage.class");
        assertProvidesCoverageLinks(result);
        assertTrue(result.getReferences().stream().anyMatch(DecompilerLinksTest::isSharedFieldReference));
    }

    @Test
    public void testCfrProvidesCoverageLinks() throws Exception {
        assertProvidesCoverageLinks(Decompilers.CFR.decompile("target/test-classes", "com/heliosdecompiler/transformerapi", "TestLinkCoverage.class"));
    }

    @Test
    public void testJadxProvidesCoverageLinks() throws Exception {
        DecompilationResult result = Decompilers.JADX.decompile("target/test-classes", "com/heliosdecompiler/transformerapi", "TestLinkCoverage.class");
        assertProvidesCoverageLinks(result);
        assertTrue(result.getReferences().stream().anyMatch(DecompilerLinksTest::isSharedFieldReference));
    }

    private static void assertProvidesBasicLinks(DecompilationResult result) {
        assertNotNull(result.getDeclarations().get("test/TestCompact"));
        assertNotNull(result.getDeclarations().get("test/TestCompact-log-(Ljava/lang/String;)V"));
        assertFalse(result.getHyperlinks().isEmpty());
        assertTrue(result.getReferences().stream().anyMatch(DecompilerLinksTest::isLogReference));
    }

    private static void assertProvidesBeanLinks(DecompilationResult result) {
        assertNotNull(result.getDeclarations().get("com/heliosdecompiler/transformerapi/TestBean"));
        assertNotNull(result.getDeclarations().get("com/heliosdecompiler/transformerapi/TestBean$InnerBean"));
        assertNotNull(result.getDeclarations().get("com/heliosdecompiler/transformerapi/TestBean$Helper"));
        assertNotNull(result.getDeclarations().get("com/heliosdecompiler/transformerapi/TestBean-name-Ljava/lang/String;"));
        assertNotNull(result.getDeclarations().get("com/heliosdecompiler/transformerapi/TestBean-helper-Lcom/heliosdecompiler/transformerapi/TestBean$Helper;"));
        assertNotNull(result.getDeclarations().get("com/heliosdecompiler/transformerapi/TestBean-<init>-(Ljava/lang/String;ILcom/heliosdecompiler/transformerapi/TestBean$Helper;)V"));
        assertTrue(result.getReferences().stream().anyMatch(DecompilerLinksTest::isBeanFieldReference));
        assertTrue(result.getReferences().stream().anyMatch(DecompilerLinksTest::isBeanConstructorReference));
        assertTrue(result.getReferences().stream().anyMatch(DecompilerLinksTest::isBaseConstructorReference));
        assertTrue(result.getReferences().stream().anyMatch(DecompilerLinksTest::isHelperConstructorReference));
    }

    private static void assertProvidesCoverageLinks(DecompilationResult result) {
        assertNotNull(result.getDeclarations().get("com/heliosdecompiler/transformerapi/TestLinkCoverage"));
        assertNotNull(result.getDeclarations().get("com/heliosdecompiler/transformerapi/TestLinkCoverage$Inner"));
        assertNotNull(result.getDeclarations().get("com/heliosdecompiler/transformerapi/TestLinkCoverage$Helper"));
        assertNotNull(result.getDeclarations().get("com/heliosdecompiler/transformerapi/TestLinkCoverage-nativeMethod-()V"));
        assertNotNull(result.getDeclarations().get("com/heliosdecompiler/transformerapi/TestLinkCoverage-varArgMethod-([Ljava/lang/String;)V"));
        assertTrue(result.getReferences().stream().anyMatch(DecompilerLinksTest::isLocalCallReference));
        assertTrue(result.getReferences().stream().anyMatch(DecompilerLinksTest::isBaseCallReference));
        assertTrue(result.getReferences().stream().anyMatch(DecompilerLinksTest::isStaticHelperCallReference));
        assertTrue(result.getReferences().stream().anyMatch(DecompilerLinksTest::isOwnerCallsReference));
    }

    private static boolean isLogReference(ReferenceData reference) {
        return "test/TestCompact".equals(reference.getTypeName())
            && "log".equals(reference.getName())
            && "(Ljava/lang/String;)V".equals(reference.getDescriptor());
    }

    private static boolean isBeanFieldReference(ReferenceData reference) {
        return "com/heliosdecompiler/transformerapi/TestBean".equals(reference.getTypeName())
            && "name".equals(reference.getName())
            && "Ljava/lang/String;".equals(reference.getDescriptor());
    }

    private static boolean isBeanConstructorReference(ReferenceData reference) {
        return "com/heliosdecompiler/transformerapi/TestBean".equals(reference.getTypeName())
            && "<init>".equals(reference.getName())
            && "(Ljava/lang/String;I)V".equals(reference.getDescriptor());
    }

    private static boolean isBaseConstructorReference(ReferenceData reference) {
        return "com/heliosdecompiler/transformerapi/TestBeanBase".equals(reference.getTypeName())
            && "<init>".equals(reference.getName())
            && "(Ljava/lang/String;)V".equals(reference.getDescriptor());
    }

    private static boolean isHelperConstructorReference(ReferenceData reference) {
        return "com/heliosdecompiler/transformerapi/TestBean$Helper".equals(reference.getTypeName())
            && "<init>".equals(reference.getName())
            && "(Ljava/lang/String;)V".equals(reference.getDescriptor());
    }

    private static boolean isLocalCallReference(ReferenceData reference) {
        return "com/heliosdecompiler/transformerapi/TestLinkCoverage".equals(reference.getTypeName())
            && "localCall".equals(reference.getName())
            && "()V".equals(reference.getDescriptor());
    }

    private static boolean isBaseCallReference(ReferenceData reference) {
        return "com/heliosdecompiler/transformerapi/TestLinkCoverageBase".equals(reference.getTypeName())
            && "baseCall".equals(reference.getName())
            && "()V".equals(reference.getDescriptor());
    }

    private static boolean isSharedFieldReference(ReferenceData reference) {
        return "com/heliosdecompiler/transformerapi/TestLinkCoverageBase".equals(reference.getTypeName())
            && "shared".equals(reference.getName())
            && "Ljava/lang/String;".equals(reference.getDescriptor());
    }

    private static boolean isStaticHelperCallReference(ReferenceData reference) {
        return "com/heliosdecompiler/transformerapi/TestLinkCoverage$Helper".equals(reference.getTypeName())
            && "staticCall".equals(reference.getName())
            && "()V".equals(reference.getDescriptor());
    }

    private static boolean isOwnerCallsReference(ReferenceData reference) {
        return "com/heliosdecompiler/transformerapi/TestLinkCoverage".equals(reference.getTypeName())
            && "ownerCalls".equals(reference.getName())
            && "()V".equals(reference.getDescriptor());
    }

    private static Loader loader(String resourcePath) throws Exception {
        try (InputStream in = DecompilerLinksTest.class.getResourceAsStream(resourcePath)) {
            byte[] data = IOUtils.toByteArray(in);
            ZipLoader zipLoader = new ZipLoader(new ByteArrayInputStream(data));
            return new Loader(zipLoader::canLoad, zipLoader::load, DecompilerLinksTest.class.getResource(resourcePath).toURI());
        }
    }
}
