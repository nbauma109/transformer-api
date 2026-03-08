/*
 * Copyright 2026 Nicolas Baumann (@nbauma109)
 *
 * Licensed under the Apache License, Version 2.0.
 */

package com.heliosdecompiler.transformerapi.common;

import java.util.Map;

import jd.core.DecompilationResult;
import jd.core.links.DeclarationData;
import jd.core.links.HyperlinkReferenceData;
import jd.core.links.ReferenceData;

/**
 * Shared helpers for writing declarations and hyperlinks into a {@link DecompilationResult}.
 *
 * @since 4.2.3
 */
public final class ResultLinkSupport {

    /**
     * Normalized symbol target used before writing JD-style link entries.
     */
    public record LinkTarget(String typeName, String name, String descriptor) {
    }

    private ResultLinkSupport() {
    }

    /**
     * Register a declaration using the key format expected by JD-style consumers.
     */
    public static void addDeclaration(DecompilationResult result, int start, int length, LinkTarget target) {
        DeclarationData data = new DeclarationData(start, length, target.typeName(), target.name(), target.descriptor());
        result.addDeclaration(declarationKey(target), data);
        if (target.name() == null) {
            result.addTypeDeclaration(start, data);
        }
    }

    /**
     * Register a hyperlink backed by a cached reference object.
     */
    public static void addReference(DecompilationResult result, Map<String, ReferenceData> cache, int start, int length, LinkTarget target, String scopeInternalName) {
        ReferenceData reference = reference(result, cache, target, scopeInternalName);
        result.addHyperLink(start, new HyperlinkReferenceData(start, length, reference));
    }

    /**
     * Resolve or create a reference so repeated mentions reuse the same instance.
     */
    public static ReferenceData reference(DecompilationResult result, Map<String, ReferenceData> cache, LinkTarget target, String scopeInternalName) {
        // A stable cache key keeps the result model compact when the same target is referenced many times.
        String key = declarationKey(target) + '-' + scopeInternalName;
        return cache.computeIfAbsent(key, ignored -> {
            ReferenceData reference = new ReferenceData(target.typeName(), target.name(), target.descriptor(), scopeInternalName);
            reference.setEnabled(true);
            result.addReference(reference);
            return reference;
        });
    }

    /**
     * Build the declaration lookup key used by {@link DecompilationResult#getDeclarations()}.
     */
    public static String declarationKey(LinkTarget target) {
        if (target.name() == null) {
            return target.typeName();
        }
        return target.typeName() + '-' + target.name() + '-' + target.descriptor();
    }

    /**
     * Measure the identifier length at a source offset.
     */
    public static int identifierLength(String code, int start) {
        int end = start;
        while (end < code.length() && Character.isJavaIdentifierPart(code.charAt(end))) {
            end++;
        }
        return end - start;
    }
}
