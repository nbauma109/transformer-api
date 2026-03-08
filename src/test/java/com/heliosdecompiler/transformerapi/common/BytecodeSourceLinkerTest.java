/*
 * Copyright 2026 Nicolas Baumann (@nbauma109)
 *
 * Licensed under the Apache License, Version 2.0.
 */

package com.heliosdecompiler.transformerapi.common;

import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import jd.core.DecompilationResult;
import jd.core.links.ReferenceData;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class BytecodeSourceLinkerTest {

    @Test
    public void testLinkIgnoresMissingSourceOrBytecode() {
        DecompilationResult result = new DecompilationResult();

        BytecodeSourceLinker.link(result, null, "com/heliosdecompiler/transformerapi/TestLinkCoverage", Map.of());
        BytecodeSourceLinker.link(result, "", "com/heliosdecompiler/transformerapi/TestLinkCoverage", importantData());

        assertTrue(result.getDeclarations().isEmpty());
        assertTrue(result.getReferences().isEmpty());
        assertTrue(result.getHyperlinks().isEmpty());
    }

    @Test
    public void testLinkSupportsInnerClassAsRootSource() {
        DecompilationResult result = new DecompilationResult();

        BytecodeSourceLinker.link(
            result,
            """
                package com.heliosdecompiler.transformerapi;

                class Inner extends InnerBaseCoverage {
                    private final String innerLabel;

                    Inner() {
                        this("coverage");
                    }

                    Inner(String innerLabel) {
                        super(innerLabel);
                        this.innerLabel = innerLabel;
                    }

                    Inner copy() {
                        return new Inner();
                    }

                    void run() {
                        this.innerCall();
                        super.baseInner();
                        TestLinkCoverage.this.ownerCalls();
                    }

                    void accept(final Inner other) {
                        other.innerCall();
                    }

                    void acceptRoot(final TestLinkCoverage other) {
                        other.ownerCalls();
                    }

                    void acceptBase(final InnerBaseCoverage base) {
                        base.baseInner();
                    }

                    void innerCall() {
                    }
                }
                """,
            "com/heliosdecompiler/transformerapi/TestLinkCoverage$Inner",
            importantData(
                "com/heliosdecompiler/transformerapi/TestLinkCoverage$Inner",
                "com/heliosdecompiler/transformerapi/TestLinkCoverage",
                "com/heliosdecompiler/transformerapi/InnerBaseCoverage"
            )
        );

        assertNotNull(result.getDeclarations().get("com/heliosdecompiler/transformerapi/TestLinkCoverage$Inner"));
        assertNotNull(result.getDeclarations().get("com/heliosdecompiler/transformerapi/TestLinkCoverage$Inner-run-()V"));
        assertNotNull(result.getDeclarations().get("com/heliosdecompiler/transformerapi/TestLinkCoverage$Inner-accept-(Lcom/heliosdecompiler/transformerapi/TestLinkCoverage$Inner;)V"));
        assertNotNull(result.getDeclarations().get("com/heliosdecompiler/transformerapi/TestLinkCoverage$Inner-acceptRoot-(Lcom/heliosdecompiler/transformerapi/TestLinkCoverage;)V"));
        assertNotNull(result.getDeclarations().get("com/heliosdecompiler/transformerapi/TestLinkCoverage$Inner-acceptBase-(Lcom/heliosdecompiler/transformerapi/InnerBaseCoverage;)V"));
        assertTrue(result.getReferences().stream().anyMatch(BytecodeSourceLinkerTest::isInnerCallReference));
        assertTrue(result.getReferences().stream().anyMatch(BytecodeSourceLinkerTest::isBaseInnerReference));
        assertTrue(result.getReferences().stream().anyMatch(BytecodeSourceLinkerTest::isOwnerCallsReference));
    }

    @Test
    public void testLinkBuildsDescriptorsForQualifiedTypesAndFinalParameters() {
        DecompilationResult result = new DecompilationResult();

        BytecodeSourceLinker.link(
            result,
            readSource("src/test/java/com/heliosdecompiler/transformerapi/TestLinkCoverage.java"),
            "com/heliosdecompiler/transformerapi/TestLinkCoverage",
            importantData(
                "com/heliosdecompiler/transformerapi/TestLinkCoverage",
                "com/heliosdecompiler/transformerapi/TestLinkCoverageBase"
            )
        );

        assertNotNull(result.getDeclarations().get("com/heliosdecompiler/transformerapi/TestLinkCoverage"));
        assertNotNull(result.getDeclarations().get("com/heliosdecompiler/transformerapi/TestLinkCoverage-nativeMethod-()V"));
        assertNotNull(result.getDeclarations().get("com/heliosdecompiler/transformerapi/TestLinkCoverage-useSupplier-(Ljava/util/function/Supplier;)V"));
        assertNotNull(result.getDeclarations().get("com/heliosdecompiler/transformerapi/TestLinkCoverage-useCollection-(Ljava/util/List;)V"));
        assertNotNull(result.getDeclarations().get("com/heliosdecompiler/transformerapi/TestLinkCoverage-useCollection-(Ljava/util/Set;)V"));
        assertNotNull(result.getDeclarations().get("com/heliosdecompiler/transformerapi/TestLinkCoverage-annotatedValue-(Ljava/lang/String;)V"));
        assertNotNull(result.getDeclarations().get("com/heliosdecompiler/transformerapi/TestLinkCoverage-annotatedValue-(Ljava/lang/Integer;)V"));
    }

    @Test
    public void testLinkResolvesRootTypeReferenceInsideInnerScope() {
        DecompilationResult result = new DecompilationResult();

        BytecodeSourceLinker.link(
            result,
            readSource("src/test/java/com/heliosdecompiler/transformerapi/TestLinkCoverage.java"),
            "com/heliosdecompiler/transformerapi/TestLinkCoverage",
            importantData(
                "com/heliosdecompiler/transformerapi/TestLinkCoverage",
                "com/heliosdecompiler/transformerapi/TestLinkCoverage$Inner",
                "com/heliosdecompiler/transformerapi/TestLinkCoverageBase",
                "com/heliosdecompiler/transformerapi/InnerBaseCoverage"
            )
        );

        assertTrue(result.getReferences().stream().anyMatch(BytecodeSourceLinkerTest::isRootTypeReference));
        assertTrue(result.getReferences().stream().anyMatch(BytecodeSourceLinkerTest::isOwnerCallsReference));
        assertNotNull(result.getDeclarations().get("com/heliosdecompiler/transformerapi/TestLinkCoverage$Inner-acceptRoot-(Lcom/heliosdecompiler/transformerapi/TestLinkCoverage;)V"));
        assertNotNull(result.getDeclarations().get("com/heliosdecompiler/transformerapi/TestLinkCoverage$Inner-acceptBase-(Lcom/heliosdecompiler/transformerapi/InnerBaseCoverage;)V"));
    }

    @Test
    public void testLinkFallsBackToRootAndAmbiguousTypeResolutionWithRealInput() {
        DecompilationResult result = new DecompilationResult();

        BytecodeSourceLinker.link(
            result,
            readSource("src/test/java/com/heliosdecompiler/transformerapi/TestTypeResolutionCoverage.java"),
            "com/heliosdecompiler/transformerapi/TestTypeResolutionCoverage",
            importantData(
                "com/heliosdecompiler/transformerapi/TestTypeResolutionCoverage",
                "com/heliosdecompiler/transformerapi/TestTypeResolutionCoverage$Inner",
                "com/heliosdecompiler/transformerapi/TestTypeResolutionCoverage$SharedType",
                "com/heliosdecompiler/transformerapi/TypeResolutionBase",
                "com/heliosdecompiler/transformerapi/TypeResolutionBase$TestTypeResolutionCoverage",
                "com/heliosdecompiler/transformerapi/TypeResolutionBase$SharedType"
            )
        );

        assertNotNull(result.getDeclarations().get("com/heliosdecompiler/transformerapi/TestTypeResolutionCoverage$Inner-acceptRoot-(Lcom/heliosdecompiler/transformerapi/TestTypeResolutionCoverage;)V"));
        assertNotNull(result.getDeclarations().get("com/heliosdecompiler/transformerapi/TestTypeResolutionCoverage$Inner-acceptAmbiguous-(Lcom/heliosdecompiler/transformerapi/TestTypeResolutionCoverage$SharedType;)V"));
        assertTrue(result.getReferences().stream().anyMatch(BytecodeSourceLinkerTest::isTypeResolutionRootReference));
        assertTrue(result.getReferences().stream().anyMatch(BytecodeSourceLinkerTest::isTypeResolutionRunReference));
    }

    @Test
    public void testLinkCountsOnlyTopLevelCommasForArrayAndGenericArguments() {
        DecompilationResult result = new DecompilationResult();

        BytecodeSourceLinker.link(
            result,
            readSource("src/test/java/com/heliosdecompiler/transformerapi/BytecodeSourceLinkerRegressionCoverage.java"),
            "com/heliosdecompiler/transformerapi/BytecodeSourceLinkerRegressionCoverage",
            importantData("com/heliosdecompiler/transformerapi/BytecodeSourceLinkerRegressionCoverage")
        );

        assertNotNull(result.getDeclarations().get("com/heliosdecompiler/transformerapi/BytecodeSourceLinkerRegressionCoverage-arrayTarget-(Ljava/lang/String;)V"));
        assertNotNull(result.getDeclarations().get("com/heliosdecompiler/transformerapi/BytecodeSourceLinkerRegressionCoverage-genericTarget-(Ljava/lang/String;)V"));
        assertTrue(result.getReferences().stream().anyMatch(BytecodeSourceLinkerTest::isRegressionArrayTargetReference));
        assertTrue(result.getReferences().stream().anyMatch(BytecodeSourceLinkerTest::isRegressionGenericTargetReference));
    }

    @Test
    public void testLinkHandlesEvenBackslashRunsBeforeClosingQuotes() {
        DecompilationResult result = new DecompilationResult();

        BytecodeSourceLinker.link(
            result,
            readSource("src/test/java/com/heliosdecompiler/transformerapi/BytecodeSourceLinkerRegressionCoverage.java"),
            "com/heliosdecompiler/transformerapi/BytecodeSourceLinkerRegressionCoverage",
            importantData("com/heliosdecompiler/transformerapi/BytecodeSourceLinkerRegressionCoverage")
        );

        assertNotNull(result.getDeclarations().get("com/heliosdecompiler/transformerapi/BytecodeSourceLinkerRegressionCoverage-afterEscapedString-()V"));
        assertNotNull(result.getDeclarations().get("com/heliosdecompiler/transformerapi/BytecodeSourceLinkerRegressionCoverage-afterEscapedTarget-()V"));
        assertTrue(result.getReferences().stream().anyMatch(BytecodeSourceLinkerTest::isRegressionEscapedTargetReference));
        assertTrue(result.getReferences().stream().anyMatch(BytecodeSourceLinkerTest::isRegressionAfterEscapedTargetReference));
    }

    private static boolean isInnerCallReference(ReferenceData reference) {
        return "com/heliosdecompiler/transformerapi/TestLinkCoverage$Inner".equals(reference.getTypeName())
            && "innerCall".equals(reference.getName())
            && "()V".equals(reference.getDescriptor());
    }

    private static boolean isBaseInnerReference(ReferenceData reference) {
        return "com/heliosdecompiler/transformerapi/InnerBaseCoverage".equals(reference.getTypeName())
            && "baseInner".equals(reference.getName())
            && "()V".equals(reference.getDescriptor());
    }

    private static boolean isOwnerCallsReference(ReferenceData reference) {
        return "com/heliosdecompiler/transformerapi/TestLinkCoverage".equals(reference.getTypeName())
            && "ownerCalls".equals(reference.getName())
            && "()V".equals(reference.getDescriptor());
    }

    private static boolean isRootTypeReference(ReferenceData reference) {
        return "com/heliosdecompiler/transformerapi/TestLinkCoverage".equals(reference.getTypeName())
            && reference.getName() == null
            && reference.getDescriptor() == null;
    }

    private static boolean isTypeResolutionRootReference(ReferenceData reference) {
        return "com/heliosdecompiler/transformerapi/TestTypeResolutionCoverage".equals(reference.getTypeName())
            && reference.getName() == null
            && reference.getDescriptor() == null;
    }

    private static boolean isTypeResolutionRunReference(ReferenceData reference) {
        return "com/heliosdecompiler/transformerapi/TestTypeResolutionCoverage".equals(reference.getTypeName())
            && "run".equals(reference.getName())
            && "()V".equals(reference.getDescriptor());
    }

    private static boolean isRegressionArrayTargetReference(ReferenceData reference) {
        return "com/heliosdecompiler/transformerapi/BytecodeSourceLinkerRegressionCoverage".equals(reference.getTypeName())
            && "arrayTarget".equals(reference.getName())
            && "(Ljava/lang/String;)V".equals(reference.getDescriptor());
    }

    private static boolean isRegressionGenericTargetReference(ReferenceData reference) {
        return "com/heliosdecompiler/transformerapi/BytecodeSourceLinkerRegressionCoverage".equals(reference.getTypeName())
            && "genericTarget".equals(reference.getName())
            && "(Ljava/lang/String;)V".equals(reference.getDescriptor());
    }

    private static boolean isRegressionEscapedTargetReference(ReferenceData reference) {
        return "com/heliosdecompiler/transformerapi/BytecodeSourceLinkerRegressionCoverage".equals(reference.getTypeName())
            && "escapedTarget".equals(reference.getName())
            && "()V".equals(reference.getDescriptor());
    }

    private static boolean isRegressionAfterEscapedTargetReference(ReferenceData reference) {
        return "com/heliosdecompiler/transformerapi/BytecodeSourceLinkerRegressionCoverage".equals(reference.getTypeName())
            && "afterEscapedTarget".equals(reference.getName())
            && "()V".equals(reference.getDescriptor());
    }

    private static Map<String, byte[]> importantData(String... internalNames) {
        Map<String, byte[]> data = new LinkedHashMap<>();
        for (String internalName : internalNames) {
            data.put(internalName, readClassBytes(internalName));
        }
        return data;
    }

    private static String readSource(String path) {
        try {
            return Files.readString(Path.of(path));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to read source " + path, e);
        }
    }

    private static byte[] readClassBytes(String internalName) {
        try {
            return Files.readAllBytes(Path.of("target/test-classes", internalName + ".class"));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to read test class " + internalName, e);
        }
    }
}
