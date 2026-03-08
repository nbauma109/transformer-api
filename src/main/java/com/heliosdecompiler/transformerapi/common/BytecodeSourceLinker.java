/*
 * Copyright 2026 Nicolas Baumann (@nbauma109)
 *
 * Licensed under the Apache License, Version 2.0.
 */

package com.heliosdecompiler.transformerapi.common;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import jd.core.DecompilationResult;
import jd.core.links.ReferenceData;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Best-effort source linker for decompilers that only provide rendered Java text.
 * Rebuilds JD-style declarations and hyperlinks from plain decompiled Java source when a decompiler
 * only returns rendered text and bytecode.
 *
 * <p>This linker is intentionally heuristic. It tokenizes the rendered source, aligns the tokens with
 * bytecode members collected from the requested type, its nested types, and any directly loaded
 * superclasses, then emits links for the symbols it can match confidently.
 *
 * <p>The implementation is precise for:
 * <ul>
 *   <li>Type declarations and most nested-type references that can be matched by simple name.</li>
 *   <li>Method and constructor declarations whose visible parameter list can be converted back to a
 *       JVM descriptor from the rendered source.</li>
 *   <li>Field declarations and explicit field accesses such as {@code this.field}, {@code Outer.this.field},
 *       {@code super.field}, and {@code Type.field}.</li>
 *   <li>Constructor calls written as {@code this(...)}, {@code super(...)}, or {@code new Type(...)}.</li>
 * </ul>
 *
 * <p>The implementation is deliberately conservative for:
 * <ul>
 *   <li>Method and constructor call references: calls are resolved from the explicit owner when one is
 *       visible and then matched primarily by visible argument count. The linker does not infer runtime
 *       argument types from expressions.</li>
 *   <li>Overloaded calls where multiple candidates share the same visible arity. In those cases the
 *       source descriptor is usually unavailable, so the link may stay unresolved or fall back to the
 *       only matching arity candidate.</li>
 *   <li>Lambdas, method references, and anonymous classes passed as call arguments. They contribute to
 *       the call arity, but the functional interface target type is not reconstructed from source, so
 *       overloads distinguished only by lambda or anonymous-class target type are not resolved precisely.</li>
 *   <li>Unqualified field accesses that could refer to locals, parameters, inherited fields, or implicit
 *       receiver fields. The linker only attaches field references when the owner is explicit in source.</li>
 *   <li>Cases that depend on imports, local variable types, generic substitutions, flow analysis, or full
 *       Java name resolution. This class does not build an AST or symbol table.</li>
 * </ul>
 *
 * <p>As a result, this class should be treated as a compatibility layer for text-only decompilers, not
 * as a full Java semantic resolver.
 *
 * @since 4.2.3
 */
public final class BytecodeSourceLinker {

    private static final String CONSTRUCTOR_NAME = "<init>";
    private static final String THIS_KEYWORD = "this";
    private static final String SUPER_KEYWORD = "super";

    private static final Map<String, String> PRIMITIVES = Map.of(
        "void", "V",
        "boolean", "Z",
        "byte", "B",
        "char", "C",
        "short", "S",
        "int", "I",
        "long", "J",
        "float", "F",
        "double", "D"
    );

    private BytecodeSourceLinker() {
    }

    /**
     * Populate declarations and references after a decompiler has produced source text.
     */
    public static void link(DecompilationResult result, String source, String rootInternalName, Map<String, byte[]> importantData) {
        if (source == null || source.isEmpty() || importantData.isEmpty()) {
            return;
        }
        BytecodeIndex index = BytecodeIndex.of(rootInternalName, importantData);
        SourceIndex sourceIndex = SourceIndex.build(source, index);
        sourceIndex.addDeclarations(result);
        sourceIndex.addReferences(result);
    }

    private record TypeInfo(
        String internalName,
        String simpleName,
        String parentInternalName,
        String superInternalName,
        boolean requiresOuterInstance,
        Map<String, FieldInfo> fields,
        Map<String, List<MethodInfo>> methods
    ) {
    }

    private record FieldInfo(
        String ownerInternalName,
        String name,
        String descriptor
    ) {
    }

    private record MethodInfo(
        String ownerInternalName,
        String displayName,
        String jvmName,
        String descriptor,
        int parameterCount
    ) {
    }

    private record ClassScope(
        String internalName,
        String simpleName,
        int declarationStart,
        int declarationLength,
        int bodyStart,
        int bodyEnd,
        int bodyDepth
    ) {
        boolean contains(int position) {
            return position >= bodyStart && position < bodyEnd;
        }
    }

    private record MethodDeclaration(
        String ownerInternalName,
        String displayName,
        String jvmName,
        String descriptor,
        int start,
        int length
    ) {
    }

    private record FieldDeclaration(
        String ownerInternalName,
        String name,
        String descriptor,
        int start,
        int length
    ) {
    }

    private record Token(
        String text,
        int start,
        int end
    ) {
    }

    private record BytecodeIndex(
        Map<String, TypeInfo> byInternalName,
        Map<String, List<TypeInfo>> bySimpleName,
        String rootInternalName
    ) {

        static BytecodeIndex of(String rootInternalName, Map<String, byte[]> importantData) {
            Map<String, TypeInfo> byInternalName = new LinkedHashMap<>();
            for (Map.Entry<String, byte[]> entry : importantData.entrySet()) {
                ClassReader reader = new ClassReader(entry.getValue());
                ClassNode classNode = new ClassNode();
                reader.accept(classNode, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
                boolean requiresOuterInstance = classNode.outerClass != null && (classNode.access & Opcodes.ACC_STATIC) == 0;
                Map<String, FieldInfo> fields = new LinkedHashMap<>();
                for (FieldNode field : classNode.fields) {
                    fields.put(field.name, new FieldInfo(classNode.name, field.name, field.desc));
                }
                Map<String, List<MethodInfo>> methods = new LinkedHashMap<>();
                for (MethodNode method : classNode.methods) {
                    // Group overloaded methods under the same JVM name for later disambiguation by arity.
                    methods.computeIfAbsent(method.name, ignored -> new ArrayList<>())
                        .add(new MethodInfo(classNode.name, displayMethodName(classNode.name, method.name), method.name, method.desc, visibleParameterCount(method, requiresOuterInstance)));
                }
                byInternalName.put(classNode.name, new TypeInfo(classNode.name, simpleName(classNode.name), parentInternalName(classNode.name), classNode.superName, requiresOuterInstance, fields, methods));
            }
            Map<String, List<TypeInfo>> bySimpleName = new HashMap<>();
            for (TypeInfo info : byInternalName.values()) {
                bySimpleName.computeIfAbsent(info.simpleName(), ignored -> new ArrayList<>()).add(info);
            }
            return new BytecodeIndex(byInternalName, bySimpleName, rootInternalName);
        }

        TypeInfo resolveDeclaredType(String simpleName, String parentInternalName) {
            List<TypeInfo> candidates = bySimpleName.get(simpleName);
            if (candidates == null) {
                return null;
            }
            for (TypeInfo candidate : candidates) {
                if (Objects.equals(candidate.parentInternalName(), parentInternalName)) {
                    return candidate;
                }
            }
            if (parentInternalName == null) {
                for (TypeInfo candidate : candidates) {
                    if (candidate.internalName().equals(rootInternalName)) {
                        return candidate;
                    }
                }
            }
            return candidates.size() == 1 ? candidates.get(0) : null;
        }

        TypeInfo resolveTypeReference(String simpleName, String currentInternalName) {
            // Prefer the current nesting scope before falling back to any matching known type.
            TypeInfo local = resolveDeclaredType(simpleName, currentInternalName);
            if (local != null) {
                return local;
            }
            List<TypeInfo> candidates = bySimpleName.get(simpleName);
            if (candidates == null) {
                return null;
            }
            for (TypeInfo candidate : candidates) {
                if (candidate.internalName().equals(rootInternalName)) {
                    return candidate;
                }
            }
            return null;
        }

        FieldInfo resolveField(String ownerInternalName, String name) {
            TypeInfo typeInfo = byInternalName.get(ownerInternalName);
            return typeInfo == null ? null : typeInfo.fields().get(name);
        }

        MethodInfo resolveMethod(String ownerInternalName, String name, int parameterCount) {
            TypeInfo typeInfo = byInternalName.get(ownerInternalName);
            if (typeInfo == null) {
                return null;
            }
            List<MethodInfo> candidates = typeInfo.methods().get(name);
            if (candidates == null) {
                return null;
            }
            if (candidates.size() == 1) {
                return candidates.get(0);
            }
            for (MethodInfo candidate : candidates) {
                if (candidate.parameterCount() == parameterCount) {
                    return candidate;
                }
            }
            return null;
        }

        MethodInfo resolveMethod(String ownerInternalName, String displayName, String signature) {
            TypeInfo typeInfo = byInternalName.get(ownerInternalName);
            List<MethodInfo> candidates = typeInfo == null ? null : typeInfo.methods().get(jvmMethodName(ownerInternalName, displayName));
            if (candidates == null) {
                return null;
            }
            for (MethodInfo candidate : candidates) {
                if (candidate.descriptor().equals(signature)) {
                    return candidate;
                }
            }
            return null;
        }

        String superInternalName(String internalName) {
            TypeInfo typeInfo = byInternalName.get(internalName);
            return typeInfo == null ? null : typeInfo.superInternalName();
        }

        private static String jvmMethodName(String ownerInternalName, String displayName) {
            return simpleName(ownerInternalName).equals(displayName) ? CONSTRUCTOR_NAME : displayName;
        }

        private static int visibleParameterCount(MethodNode method, boolean requiresOuterInstance) {
            int parameterCount = Type.getArgumentTypes(method.desc).length;
            if (requiresOuterInstance && CONSTRUCTOR_NAME.equals(method.name) && parameterCount > 0) {
                return parameterCount - 1;
            }
            return parameterCount;
        }
    }

    private record SourceIndex(
        BytecodeIndex bytecodeIndex,
        List<Token> tokens,
        List<ClassScope> classScopes,
        Map<Integer, FieldDeclaration> fieldDeclarationsByStart,
        Map<Integer, MethodDeclaration> methodDeclarationsByStart,
        Map<Integer, Integer> declarationLengths
    ) {

        static SourceIndex build(String source, BytecodeIndex bytecodeIndex) {
            String masked = maskNonCode(source);
            List<Token> tokens = tokenize(masked);
            List<ClassScope> classScopes = new ArrayList<>();
            Map<Integer, FieldDeclaration> fieldDeclarationsByStart = new LinkedHashMap<>();
            Map<Integer, MethodDeclaration> methodDeclarationsByStart = new LinkedHashMap<>();
            Map<Integer, Integer> declarationLengths = new HashMap<>();
            Deque<PendingClass> pendingClasses = new ArrayDeque<>();
            Deque<OpenClass> openClasses = new ArrayDeque<>();
            BuildState buildState = new BuildState(declarationLengths, pendingClasses, openClasses, classScopes, fieldDeclarationsByStart, methodDeclarationsByStart);
            int braceDepth = 0;
            /* First pass: collect the declarations that appear directly in the rendered source. */
            for (int i = 0; i < tokens.size(); i++) {
                braceDepth = handleBuildToken(tokens, i, bytecodeIndex, buildState, braceDepth);
            }
            classScopes.sort(Comparator.comparingInt(ClassScope::bodyStart).thenComparingInt(ClassScope::bodyEnd));
            return new SourceIndex(bytecodeIndex, tokens, classScopes, fieldDeclarationsByStart, methodDeclarationsByStart, declarationLengths);
        }

        void addDeclarations(DecompilationResult result) {
            for (ClassScope classScope : classScopes) {
                ResultLinkSupport.addDeclaration(result, classScope.declarationStart(), classScope.declarationLength(), new ResultLinkSupport.LinkTarget(classScope.internalName(), null, null));
            }
            for (FieldDeclaration field : fieldDeclarationsByStart.values()) {
                ResultLinkSupport.addDeclaration(result, field.start(), field.length(), target(field.ownerInternalName(), field.name(), field.descriptor()));
            }
            for (MethodDeclaration method : methodDeclarationsByStart.values()) {
                ResultLinkSupport.addDeclaration(result, method.start(), method.length(), target(method.ownerInternalName(), method.jvmName(), method.descriptor()));
            }
        }

        void addReferences(DecompilationResult result) {
            Map<String, jd.core.links.ReferenceData> references = new HashMap<>();
            for (int i = 0; i < tokens.size(); i++) {
                addReferenceForToken(result, references, i);
            }
        }

        private boolean addMethodReference(DecompilationResult result, Map<String, jd.core.links.ReferenceData> references, String scopeInternalName, Token token, String ownerInternalName, int parameterCount) {
            MethodInfo methodInfo = bytecodeIndex.resolveMethod(ownerInternalName, token.text(), parameterCount);
            if (methodInfo == null) {
                return false;
            }
            ResultLinkSupport.addReference(result, references, token.start(), token.end() - token.start(), target(methodInfo.ownerInternalName(), methodInfo.jvmName(), methodInfo.descriptor()), scopeInternalName);
            return true;
        }

        private ClassScope enclosingClass(int position) {
            ClassScope match = null;
            for (ClassScope scope : classScopes) {
                if (scope.contains(position) && isMoreSpecificScope(match, scope)) {
                    match = scope;
                }
            }
            return match;
        }

        private ClassScope outerClass(ClassScope scope) {
            for (ClassScope candidate : classScopes) {
                if (candidate.bodyStart() < scope.bodyStart() && candidate.bodyEnd() > scope.bodyEnd()) {
                    return candidate;
                }
            }
            return null;
        }

        private String explicitMethodOwner(Token previous, Token previousPrevious, ClassScope ownerScope) {
            String previousText = textOf(previous);
            String previousPreviousText = textOf(previousPrevious);
            if (".".equals(previousText) && THIS_KEYWORD.equals(previousPreviousText)) {
                return qualifiedThisOwner(tokens.indexOf(previousPrevious), ownerScope);
            }
            if (".".equals(previousText) && SUPER_KEYWORD.equals(previousPreviousText)) {
                return bytecodeIndex.superInternalName(ownerScope.internalName());
            }
            if (".".equals(previousText) && previousPreviousText != null) {
                TypeInfo typeInfo = bytecodeIndex.resolveTypeReference(previousPreviousText, ownerScope.internalName());
                if (typeInfo != null) {
                    return typeInfo.internalName();
                }
            }
            TypeInfo typeInfo = bytecodeIndex.resolveTypeReference(previousText, ownerScope.internalName());
            return typeInfo == null ? null : typeInfo.internalName();
        }

        private void addReferenceForToken(DecompilationResult result, Map<String, ReferenceData> references, int index) {
            Token token = tokens.get(index);
            if (isLinkableToken(token)) {
                ClassScope ownerScope = enclosingClass(token.start());
                if (ownerScope != null) {
                    if (looksLikeMethodCall(tokens, index)) {
                        addMethodCallReference(result, references, index, token, ownerScope);
                    } else {
                        if (!addFieldReference(result, references, index, token, ownerScope)) {
                            addTypeReference(result, references, token, ownerScope);
                        }
                    }
                }
            }
        }

        private boolean isLinkableToken(Token token) {
            return isIdentifier(token.text()) && !declarationLengths.containsKey(token.start());
        }

        private void addMethodCallReference(DecompilationResult result, Map<String, ReferenceData> references, int index, Token token, ClassScope ownerScope) {
            if (addConstructorReference(result, references, index, token, ownerScope)) {
                return;
            }
            String owner = resolveMethodOwner(index, ownerScope);
            int parameterCount = countCallArguments(tokens, index);
            boolean resolved = owner != null && addMethodReference(result, references, ownerScope.internalName(), token, owner, parameterCount);
            if (!resolved) {
                resolved = addMethodReference(result, references, ownerScope.internalName(), token, ownerScope.internalName(), parameterCount);
            }
            if (!resolved) {
                addOuterMethodReference(result, references, token, ownerScope, parameterCount);
            }
        }

        private String resolveMethodOwner(int index, ClassScope ownerScope) {
            Token previous = previousSignificant(tokens, index);
            Token previousPrevious = previousSignificant(tokens, index - 1);
            return explicitMethodOwner(previous, previousPrevious, ownerScope);
        }

        private boolean addConstructorReference(DecompilationResult result, Map<String, ReferenceData> references, int index, Token token, ClassScope ownerScope) {
            String ownerInternalName = constructorOwner(index, token, ownerScope);
            if (ownerInternalName == null) {
                return false;
            }
            MethodInfo constructor = bytecodeIndex.resolveMethod(ownerInternalName, CONSTRUCTOR_NAME, countCallArguments(tokens, index));
            if (constructor == null) {
                return false;
            }
            ResultLinkSupport.addReference(result, references, token.start(), token.end() - token.start(), target(constructor.ownerInternalName(), constructor.jvmName(), constructor.descriptor()), ownerScope.internalName());
            return true;
        }

        private String constructorOwner(int index, Token token, ClassScope ownerScope) {
            String tokenText = token.text();
            if (THIS_KEYWORD.equals(tokenText)) {
                return ownerScope.internalName();
            }
            if (SUPER_KEYWORD.equals(tokenText)) {
                return bytecodeIndex.superInternalName(ownerScope.internalName());
            }
            Token previous = previousSignificant(tokens, index);
            if (previous == null || !"new".equals(previous.text())) {
                return null;
            }
            TypeInfo typeInfo = bytecodeIndex.resolveTypeReference(tokenText, ownerScope.internalName());
            return typeInfo == null ? null : typeInfo.internalName();
        }

        private void addOuterMethodReference(DecompilationResult result, Map<String, ReferenceData> references, Token token, ClassScope ownerScope, int parameterCount) {
            ClassScope outerScope = outerClass(ownerScope);
            while (outerScope != null && !addMethodReference(result, references, ownerScope.internalName(), token, outerScope.internalName(), parameterCount)) {
                outerScope = outerClass(outerScope);
            }
        }

        private boolean addFieldReference(DecompilationResult result, Map<String, ReferenceData> references, int index, Token token, ClassScope ownerScope) {
            String ownerInternalName = fieldOwner(index, ownerScope);
            if (ownerInternalName == null) {
                return false;
            }
            FieldInfo fieldInfo = bytecodeIndex.resolveField(ownerInternalName, token.text());
            if (fieldInfo == null) {
                return false;
            }
            ResultLinkSupport.addReference(result, references, token.start(), token.end() - token.start(), target(fieldInfo.ownerInternalName(), fieldInfo.name(), fieldInfo.descriptor()), ownerScope.internalName());
            return true;
        }

        private String fieldOwner(int index, ClassScope ownerScope) {
            Token previous = previousSignificant(tokens, index);
            if (previous == null || !".".equals(previous.text())) {
                return null;
            }
            Token qualifier = previousSignificant(tokens, index - 1);
            String qualifierText = textOf(qualifier);
            if (THIS_KEYWORD.equals(qualifierText)) {
                return qualifiedThisOwner(tokens.indexOf(qualifier), ownerScope);
            }
            if (SUPER_KEYWORD.equals(qualifierText)) {
                return bytecodeIndex.superInternalName(ownerScope.internalName());
            }
            TypeInfo typeInfo = bytecodeIndex.resolveTypeReference(qualifierText, ownerScope.internalName());
            return typeInfo == null ? null : typeInfo.internalName();
        }

        private String qualifiedThisOwner(int thisIndex, ClassScope ownerScope) {
            Token dotBeforeThis = previousSignificant(tokens, thisIndex);
            Token typeToken = previousSignificant(tokens, thisIndex - 1);
            if (dotBeforeThis != null && ".".equals(dotBeforeThis.text()) && typeToken != null) {
                TypeInfo typeInfo = bytecodeIndex.resolveTypeReference(typeToken.text(), ownerScope.internalName());
                if (typeInfo != null) {
                    return typeInfo.internalName();
                }
            }
            return ownerScope.internalName();
        }

        private void addTypeReference(DecompilationResult result, Map<String, ReferenceData> references, Token token, ClassScope ownerScope) {
            TypeInfo typeInfo = bytecodeIndex.resolveTypeReference(token.text(), ownerScope.internalName());
            if (typeInfo != null) {
                ResultLinkSupport.addReference(result, references, token.start(), token.end() - token.start(), target(typeInfo.internalName(), null, null), ownerScope.internalName());
            }
        }
    }

    private record PendingClass(
        String internalName,
        String simpleName,
        int declarationStart,
        int declarationLength
    ) {
    }

    private record OpenClass(
        String internalName,
        String simpleName,
        int declarationStart,
        int declarationLength,
        int bodyStart,
        int bodyDepth
    ) {
    }

    private record BuildState(
        Map<Integer, Integer> declarationLengths,
        Deque<PendingClass> pendingClasses,
        Deque<OpenClass> openClasses,
        List<ClassScope> classScopes,
        Map<Integer, FieldDeclaration> fieldDeclarationsByStart,
        Map<Integer, MethodDeclaration> methodDeclarationsByStart
    ) {
    }

    private record ParameterType(
        String rawType,
        int arrayDepth,
        boolean varArgs
    ) {
    }

    private static MethodDeclaration tryCreateMethodDeclaration(List<Token> tokens, int index, OpenClass currentClass, BytecodeIndex bytecodeIndex, Map<Integer, Integer> declarationLengths) {
        Token nameToken = tokens.get(index);
        if (!startsMethodDeclaration(tokens, index)) {
            return null;
        }
        if (isQualifiedMethodName(tokens, index)) {
            return null;
        }
        int closeIndex = findMatchingParen(tokens, index + 1);
        Token after = declarationTerminator(tokens, closeIndex);
        if (after == null || (!"{".equals(after.text()) && !";".equals(after.text()))) {
            return null;
        }
        String signature = buildDescriptor(tokens, index + 1, closeIndex, currentClass.internalName(), bytecodeIndex);
        MethodInfo methodInfo = signature == null ? null : bytecodeIndex.resolveMethod(currentClass.internalName(), nameToken.text(), signature);
        if (methodInfo == null) {
            methodInfo = bytecodeIndex.resolveMethod(currentClass.internalName(), currentClass.simpleName().equals(nameToken.text()) ? CONSTRUCTOR_NAME : nameToken.text(), countParameters(tokens, index + 1, closeIndex));
        }
        if (methodInfo == null) {
            return null;
        }
        declarationLengths.put(nameToken.start(), nameToken.end() - nameToken.start());
        return new MethodDeclaration(methodInfo.ownerInternalName(), nameToken.text(), methodInfo.jvmName(), methodInfo.descriptor(), nameToken.start(), nameToken.end() - nameToken.start());
    }

    private static String buildDescriptor(List<Token> tokens, int openParenIndex, int closeParenIndex, String currentInternalName, BytecodeIndex bytecodeIndex) {
        List<String> parameterDescriptors = new ArrayList<>();
        List<Token> current = new ArrayList<>();
        int genericDepth = 0;
        for (int i = openParenIndex + 1; i < closeParenIndex; i++) {
            genericDepth = consumeParameterToken(tokens.get(i), current, genericDepth, parameterDescriptors, currentInternalName, bytecodeIndex);
        }
        addParameterDescriptor(current, parameterDescriptors, currentInternalName, bytecodeIndex);
        StringBuilder descriptor = new StringBuilder();
        descriptor.append('(');
        for (String parameterDescriptor : parameterDescriptors) {
            descriptor.append(parameterDescriptor);
        }
        descriptor.append(')');
        descriptor.append('V');
        return descriptor.toString();
    }

    private static String toParameterDescriptor(List<Token> tokens, String currentInternalName, BytecodeIndex bytecodeIndex) {
        ParameterType parameterType = parameterType(tokens);
        int arrayDepth = parameterType.arrayDepth() + (parameterType.varArgs() ? 1 : 0);
        return "[".repeat(arrayDepth) + typeDescriptor(parameterType.rawType(), currentInternalName, bytecodeIndex);
    }

    private static String typeDescriptor(String rawType, String currentInternalName, BytecodeIndex bytecodeIndex) {
        String primitive = PRIMITIVES.get(rawType);
        if (primitive != null) {
            return primitive;
        }
        TypeInfo typeInfo = resolveKnownType(rawType, currentInternalName, bytecodeIndex);
        if (typeInfo != null) {
            return 'L' + typeInfo.internalName() + ';';
        }
        if (rawType.contains(".")) {
            return 'L' + rawType.replace('.', '/') + ';';
        }
        return 'L' + rawType + ';';
    }

    private static int countParameters(List<Token> tokens, int openParenIndex, int closeParenIndex) {
        if (openParenIndex + 1 == closeParenIndex) {
            return 0;
        }
        int count = 1;
        int genericDepth = 0;
        for (int i = openParenIndex + 1; i < closeParenIndex; i++) {
            String text = tokens.get(i).text();
            if ("<".equals(text)) {
                genericDepth++;
            } else if (">".equals(text) && genericDepth > 0) {
                genericDepth--;
            } else if (",".equals(text) && genericDepth == 0) {
                count++;
            }
        }
        return count;
    }

    private static boolean looksLikeMethodCall(List<Token> tokens, int index) {
        Token next = nextSignificant(tokens, index);
        if (next == null || !"(".equals(next.text())) {
            return false;
        }
        Token previous = previousSignificant(tokens, index);
        return previous == null || !isTypeKeyword(previous.text());
    }

    private static int countCallArguments(List<Token> tokens, int nameIndex) {
        int openIndex = nameIndex + 1;
        if (openIndex >= tokens.size() || !"(".equals(tokens.get(openIndex).text())) {
            return 0;
        }
        int closeIndex = findMatchingParen(tokens, openIndex);
        if (closeIndex < 0 || openIndex + 1 == closeIndex) {
            return 0;
        }
        CallArgumentNesting nesting = new CallArgumentNesting();
        int count = 1;
        for (int i = openIndex + 1; i < closeIndex; i++) {
            if (",".equals(tokens.get(i).text()) && nesting.isTopLevel()) {
                count++;
                continue;
            }
            nesting.accept(tokens, i, closeIndex);
        }
        return count;
    }

    private static boolean startsGenericArgumentList(List<Token> tokens, int openIndex, int limitIndex) {
        Token previous = previousSignificant(tokens, openIndex);
        Token next = nextSignificant(tokens, openIndex);
        if (previous == null || next == null) {
            return false;
        }
        String previousText = previous.text();
        String nextText = next.text();
        if (!".".equals(previousText)
            && !isIdentifier(previousText)
            && !"?".equals(previousText)
            && !">".equals(previousText)
            && !"]".equals(previousText)) {
            return false;
        }
        if (!isIdentifier(nextText) && !"?".equals(nextText)) {
            return false;
        }
        int closeIndex = findMatchingAngle(tokens, openIndex, limitIndex);
        if (closeIndex < 0) {
            return false;
        }
        Token after = nextSignificant(tokens, closeIndex);
        String afterText = textOf(after);
        if (".".equals(previousText)) {
            return isIdentifier(afterText) || "(".equals(afterText);
        }
        return "(".equals(afterText)
            || "[".equals(afterText)
            || ")".equals(afterText)
            || ",".equals(afterText)
            || ".".equals(afterText)
            || ";".equals(afterText)
            || ":".equals(afterText)
            || "}".equals(afterText);
    }

    private static int findMatchingAngle(List<Token> tokens, int openIndex, int limitIndex) {
        int depth = 0;
        for (int i = openIndex; i < limitIndex; i++) {
            String text = tokens.get(i).text();
            if ("<".equals(text)) {
                depth++;
            } else if (">".equals(text)) {
                depth--;
                if (depth == 0) {
                    return i;
                }
            }
        }
        return -1;
    }

    private static int findMatchingParen(List<Token> tokens, int openIndex) {
        int depth = 0;
        for (int i = openIndex; i < tokens.size(); i++) {
            String text = tokens.get(i).text();
            if ("(".equals(text)) {
                depth++;
            } else if (")".equals(text)) {
                depth--;
                if (depth == 0) {
                    return i;
                }
            }
        }
        return -1;
    }

    private static Token nextIdentifier(List<Token> tokens, int startIndex) {
        for (int i = startIndex; i < tokens.size(); i++) {
            if (isIdentifier(tokens.get(i).text())) {
                return tokens.get(i);
            }
        }
        return null;
    }

    private static Token nextSignificant(List<Token> tokens, int index) {
        int nextIndex = index + 1;
        return nextIndex < tokens.size() ? tokens.get(nextIndex) : null;
    }

    private static Token previousSignificant(List<Token> tokens, int index) {
        int previousIndex = index - 1;
        return previousIndex >= 0 ? tokens.get(previousIndex) : null;
    }

    private static String textOf(Token token) {
        return token == null ? null : token.text();
    }

    private static List<Token> tokenize(String source) {
        List<Token> tokens = new ArrayList<>();
        int i = 0;
        while (i < source.length()) {
            TokenMatch match = nextToken(source, i);
            if (match.token() != null) {
                tokens.add(match.token());
            }
            i = match.nextIndex();
        }
        return tokens;
    }

    private static String maskNonCode(String source) {
        char[] chars = source.toCharArray();
        MaskState state = MaskState.CODE;
        /*
         * Blank comments and literals so token offsets stay aligned with the original source while
         * the token scanner only sees structural Java syntax.
         */
        for (int i = 0; i < chars.length; i++) {
            state = switch (state) {
                case CODE -> handleCodeState(chars, i);
                case LINE_COMMENT -> handleLineCommentState(chars, i);
                case BLOCK_COMMENT -> handleBlockCommentState(chars, i);
                case STRING -> handleQuotedState(chars, i, '"', MaskState.STRING);
                case CHAR -> handleQuotedState(chars, i, '\'', MaskState.CHAR);
            };
        }
        return new String(chars);
    }

    private static boolean isIdentifier(String text) {
        return !text.isEmpty() && Character.isJavaIdentifierStart(text.charAt(0));
    }

    private static boolean isTypeKeyword(String text) {
        return "class".equals(text) || "interface".equals(text) || "enum".equals(text) || "record".equals(text);
    }

    private static boolean isAnnotation(String text) {
        return "@".equals(text) || text.startsWith("@");
    }

    private static boolean isModifier(String text) {
        return switch (text) {
            case "public", "protected", "private", "static", "final", "abstract", "synchronized", "native", "strictfp", "default", "transient", "volatile" -> true;
            default -> false;
        };
    }

    private static String displayMethodName(String internalName, String name) {
        return CONSTRUCTOR_NAME.equals(name) ? simpleName(internalName) : name;
    }

    private static String simpleName(String internalName) {
        int inner = internalName.lastIndexOf('$');
        if (inner >= 0) {
            return internalName.substring(inner + 1);
        }
        int pkg = internalName.lastIndexOf('/');
        return pkg >= 0 ? internalName.substring(pkg + 1) : internalName;
    }

    private static String parentInternalName(String internalName) {
        int inner = internalName.lastIndexOf('$');
        return inner >= 0 ? internalName.substring(0, inner) : null;
    }

    private static int handleBuildToken(List<Token> tokens, int index, BytecodeIndex bytecodeIndex, BuildState buildState, int braceDepth) {
        Token token = tokens.get(index);
        String tokenText = token.text();
        if (isTypeKeyword(tokenText)) {
            registerTypeDeclaration(tokens, index, bytecodeIndex, buildState);
            return braceDepth;
        }
        if ("{".equals(tokenText)) {
            openPendingClass(token, braceDepth, buildState);
            return braceDepth + 1;
        }
        if ("}".equals(tokenText)) {
            closeClassScope(token, braceDepth, buildState);
            return braceDepth - 1;
        }
        registerMethodDeclaration(tokens, index, bytecodeIndex, buildState, braceDepth);
        registerFieldDeclaration(tokens, index, bytecodeIndex, buildState, braceDepth);
        return braceDepth;
    }

    private static void registerTypeDeclaration(List<Token> tokens, int index, BytecodeIndex bytecodeIndex, BuildState buildState) {
        Token nameToken = nextIdentifier(tokens, index + 1);
        if (nameToken != null) {
            String parentInternalName = buildState.openClasses().isEmpty() ? null : buildState.openClasses().peek().internalName();
            TypeInfo typeInfo = bytecodeIndex.resolveDeclaredType(nameToken.text(), parentInternalName);
            if (typeInfo != null) {
                buildState.declarationLengths().put(nameToken.start(), nameToken.end() - nameToken.start());
                buildState.pendingClasses().push(new PendingClass(typeInfo.internalName(), typeInfo.simpleName(), nameToken.start(), nameToken.end() - nameToken.start()));
            }
        }
    }

    private static void openPendingClass(Token token, int braceDepth, BuildState buildState) {
        if (!buildState.pendingClasses().isEmpty()) {
            PendingClass pending = buildState.pendingClasses().pop();
            buildState.openClasses().push(new OpenClass(pending.internalName(), pending.simpleName(), pending.declarationStart(), pending.declarationLength(), token.end(), braceDepth + 1));
        }
    }

    private static void closeClassScope(Token token, int braceDepth, BuildState buildState) {
        if (!buildState.openClasses().isEmpty() && buildState.openClasses().peek().bodyDepth() == braceDepth) {
            OpenClass open = buildState.openClasses().pop();
            buildState.classScopes().add(new ClassScope(open.internalName(), open.simpleName(), open.declarationStart(), open.declarationLength(), open.bodyStart(), token.start(), open.bodyDepth()));
        }
    }

    private static void registerMethodDeclaration(List<Token> tokens, int index, BytecodeIndex bytecodeIndex, BuildState buildState, int braceDepth) {
        if (!buildState.openClasses().isEmpty() && braceDepth == buildState.openClasses().peek().bodyDepth() && isIdentifier(tokens.get(index).text())) {
            MethodDeclaration declaration = tryCreateMethodDeclaration(tokens, index, buildState.openClasses().peek(), bytecodeIndex, buildState.declarationLengths());
            if (declaration != null) {
                buildState.methodDeclarationsByStart().put(declaration.start(), declaration);
            }
        }
    }

    private static void registerFieldDeclaration(List<Token> tokens, int index, BytecodeIndex bytecodeIndex, BuildState buildState, int braceDepth) {
        if (!buildState.openClasses().isEmpty() && braceDepth == buildState.openClasses().peek().bodyDepth() && isIdentifier(tokens.get(index).text())) {
            FieldDeclaration declaration = tryCreateFieldDeclaration(tokens, index, buildState.openClasses().peek(), bytecodeIndex, buildState.declarationLengths());
            if (declaration != null) {
                buildState.fieldDeclarationsByStart().put(declaration.start(), declaration);
            }
        }
    }

    private static FieldDeclaration tryCreateFieldDeclaration(List<Token> tokens, int index, OpenClass currentClass, BytecodeIndex bytecodeIndex, Map<Integer, Integer> declarationLengths) {
        Token nameToken = tokens.get(index);
        if (startsMethodDeclaration(tokens, index) || isQualifiedFieldName(tokens, index) || !hasFieldTerminator(tokens, index)) {
            return null;
        }
        FieldInfo fieldInfo = bytecodeIndex.resolveField(currentClass.internalName(), nameToken.text());
        if (fieldInfo == null) {
            return null;
        }
        declarationLengths.put(nameToken.start(), nameToken.end() - nameToken.start());
        return new FieldDeclaration(fieldInfo.ownerInternalName(), fieldInfo.name(), fieldInfo.descriptor(), nameToken.start(), nameToken.end() - nameToken.start());
    }

    private static boolean isMoreSpecificScope(ClassScope currentMatch, ClassScope candidate) {
        return currentMatch == null || (candidate.bodyStart() >= currentMatch.bodyStart() && candidate.bodyEnd() <= currentMatch.bodyEnd());
    }

    private static boolean startsMethodDeclaration(List<Token> tokens, int index) {
        Token next = nextSignificant(tokens, index);
        return next != null && "(".equals(next.text());
    }

    private static boolean isQualifiedMethodName(List<Token> tokens, int index) {
        String previousText = textOf(previousSignificant(tokens, index));
        return "new".equals(previousText) || ".".equals(previousText);
    }

    private static boolean isQualifiedFieldName(List<Token> tokens, int index) {
        return ".".equals(textOf(previousSignificant(tokens, index)));
    }

    private static boolean hasFieldTerminator(List<Token> tokens, int index) {
        Token next = nextSignificant(tokens, index);
        return next != null && ";".equals(next.text());
    }

    private static Token declarationTerminator(List<Token> tokens, int closeIndex) {
        if (closeIndex < 0) {
            return null;
        }
        Token after = nextSignificant(tokens, closeIndex);
        while (after != null && "throws".equals(after.text())) {
            after = nextTypeBoundary(tokens, tokens.indexOf(after));
        }
        return after;
    }

    private static void addParameterDescriptor(List<Token> current, List<String> parameterDescriptors, String currentInternalName, BytecodeIndex bytecodeIndex) {
        if (!current.isEmpty()) {
            parameterDescriptors.add(toParameterDescriptor(current, currentInternalName, bytecodeIndex));
            current.clear();
        }
    }

    private static Token nextTypeBoundary(List<Token> tokens, int startIndex) {
        for (int i = startIndex + 1; i < tokens.size(); i++) {
            Token candidate = tokens.get(i);
            if ("{".equals(candidate.text()) || ";".equals(candidate.text())) {
                return candidate;
            }
        }
        return null;
    }

    private static int consumeParameterToken(Token token, List<Token> current, int genericDepth, List<String> parameterDescriptors, String currentInternalName, BytecodeIndex bytecodeIndex) {
        String tokenText = token.text();
        if ("<".equals(tokenText)) {
            current.add(token);
            return genericDepth + 1;
        }
        if (">".equals(tokenText) && genericDepth > 0) {
            current.add(token);
            return genericDepth - 1;
        }
        if (",".equals(tokenText) && genericDepth == 0) {
            addParameterDescriptor(current, parameterDescriptors, currentInternalName, bytecodeIndex);
            return genericDepth;
        }
        current.add(token);
        return genericDepth;
    }

    private static TypeInfo resolveKnownType(String rawType, String currentInternalName, BytecodeIndex bytecodeIndex) {
        TypeInfo typeInfo = bytecodeIndex.resolveTypeReference(rawType, currentInternalName);
        if (typeInfo != null) {
            return typeInfo;
        }
        int separator = rawType.lastIndexOf('.');
        if (separator < 0) {
            return null;
        }
        return bytecodeIndex.resolveTypeReference(rawType.substring(separator + 1), currentInternalName);
    }

    private static ParameterType parameterType(List<Token> tokens) {
        List<String> typeTokens = new ArrayList<>();
        int arrayDepth = 0;
        boolean varArgs = false;
        int index = 0;
        while (index < tokens.size()) {
            String tokenText = tokens.get(index).text();
            if (isAnnotation(tokenText)) {
                index = skipAnnotation(tokens, index) + 1;
            } else if ("[".equals(tokenText)) {
                arrayDepth++;
                index++;
            } else if ("...".equals(tokenText)) {
                varArgs = true;
                index++;
            } else if (!isModifier(tokenText)) {
                typeTokens.add(tokenText);
                index++;
            } else {
                index++;
            }
        }
        return new ParameterType(rawParameterType(typeTokens), arrayDepth, varArgs);
    }

    private static int skipAnnotation(List<Token> tokens, int index) {
        int currentIndex = index + 1;
        while (currentIndex < tokens.size()) {
            String tokenText = tokens.get(currentIndex).text();
            if (!".".equals(tokenText) && !isIdentifier(tokenText)) {
                break;
            }
            currentIndex++;
        }
        if (currentIndex < tokens.size() && "(".equals(tokens.get(currentIndex).text())) {
            int closeIndex = findMatchingParen(tokens, currentIndex);
            return closeIndex >= 0 ? closeIndex : tokens.size() - 1;
        }
        return currentIndex - 1;
    }

    private static String rawParameterType(List<String> typeTokens) {
        if (typeTokens.size() < 2) {
            return "java.lang.Object";
        }
        StringBuilder rawType = new StringBuilder();
        int genericDepth = 0;
        for (int i = 0; i < typeTokens.size() - 1; i++) {
            String tokenText = typeTokens.get(i);
            if ("<".equals(tokenText)) {
                genericDepth++;
            } else if (">".equals(tokenText) && genericDepth > 0) {
                genericDepth--;
            } else if (genericDepth == 0) {
                if (".".equals(tokenText)) {
                    rawType.append('.');
                } else {
                    rawType.append(tokenText);
                }
            }
        }
        return rawType.isEmpty() ? "java.lang.Object" : rawType.toString();
    }

    private static TokenMatch nextToken(String source, int index) {
        char current = source.charAt(index);
        if (Character.isWhitespace(current)) {
            return new TokenMatch(null, index + 1);
        }
        if (Character.isJavaIdentifierStart(current)) {
            return identifierToken(source, index);
        }
        if (isVarArgs(source, index)) {
            return new TokenMatch(new Token("...", index, index + 3), index + 3);
        }
        return new TokenMatch(new Token(String.valueOf(current), index, index + 1), index + 1);
    }

    private static TokenMatch identifierToken(String source, int index) {
        int end = index + 1;
        while (end < source.length() && Character.isJavaIdentifierPart(source.charAt(end))) {
            end++;
        }
        return new TokenMatch(new Token(source.substring(index, end), index, end), end);
    }

    private static boolean isVarArgs(String source, int index) {
        return source.charAt(index) == '.' && index + 2 < source.length() && source.charAt(index + 1) == '.' && source.charAt(index + 2) == '.';
    }

    private static MaskState handleCodeState(char[] chars, int index) {
        if (startsLineComment(chars, index)) {
            blankPair(chars, index);
            return MaskState.LINE_COMMENT;
        }
        if (startsBlockComment(chars, index)) {
            blankPair(chars, index);
            return MaskState.BLOCK_COMMENT;
        }
        if (chars[index] == '"') {
            chars[index] = ' ';
            return MaskState.STRING;
        }
        if (chars[index] == '\'') {
            chars[index] = ' ';
            return MaskState.CHAR;
        }
        return MaskState.CODE;
    }

    private static MaskState handleLineCommentState(char[] chars, int index) {
        if (chars[index] == '\n') {
            return MaskState.CODE;
        }
        chars[index] = ' ';
        return MaskState.LINE_COMMENT;
    }

    private static MaskState handleBlockCommentState(char[] chars, int index) {
        if (endsBlockComment(chars, index)) {
            blankPair(chars, index);
            return MaskState.CODE;
        }
        if (chars[index] != '\n') {
            chars[index] = ' ';
        }
        return MaskState.BLOCK_COMMENT;
    }

    private static MaskState handleQuotedState(char[] chars, int index, char quote, MaskState quotedState) {
        boolean closingQuote = chars[index] == quote && !isEscaped(chars, index);
        if (chars[index] != '\n') {
            chars[index] = ' ';
        }
        return closingQuote ? MaskState.CODE : quotedState;
    }

    private static boolean startsLineComment(char[] chars, int index) {
        return chars[index] == '/' && index + 1 < chars.length && chars[index + 1] == '/';
    }

    private static boolean startsBlockComment(char[] chars, int index) {
        return chars[index] == '/' && index + 1 < chars.length && chars[index + 1] == '*';
    }

    private static boolean endsBlockComment(char[] chars, int index) {
        return chars[index] == '*' && index + 1 < chars.length && chars[index + 1] == '/';
    }

    private static void blankPair(char[] chars, int index) {
        chars[index] = ' ';
        if (index + 1 < chars.length) {
            chars[index + 1] = ' ';
        }
    }

    private static boolean isEscaped(char[] chars, int index) {
        int backslashCount = 0;
        for (int i = index - 1; i >= 0 && chars[i] == '\\'; i--) {
            backslashCount++;
        }
        return (backslashCount & 1) == 1;
    }

    private static final class CallArgumentNesting {
        private int parenDepth;
        private int braceDepth;
        private int bracketDepth;
        private int angleDepth;

        private boolean isTopLevel() {
            return parenDepth == 0 && braceDepth == 0 && bracketDepth == 0 && angleDepth == 0;
        }

        private void accept(List<Token> tokens, int index, int closeIndex) {
            switch (tokens.get(index).text()) {
                case "<" -> {
                    if (startsGenericArgumentList(tokens, index, closeIndex)) {
                        angleDepth++;
                    }
                }
                case ">" -> decreaseAngleDepth();
                case "(" -> parenDepth++;
                case ")" -> parenDepth = decreaseDepth(parenDepth);
                case "{" -> braceDepth++;
                case "}" -> braceDepth = decreaseDepth(braceDepth);
                case "[" -> bracketDepth++;
                case "]" -> bracketDepth = decreaseDepth(bracketDepth);
                default -> {
                }
            }
        }

        private void decreaseAngleDepth() {
            if (angleDepth > 0) {
                angleDepth--;
            }
        }

        private static int decreaseDepth(int depth) {
            return depth > 0 ? depth - 1 : 0;
        }
    }

    private static ResultLinkSupport.LinkTarget target(String typeName, String name, String descriptor) {
        return new ResultLinkSupport.LinkTarget(typeName, name, descriptor);
    }

    private enum MaskState {
        CODE,
        LINE_COMMENT,
        BLOCK_COMMENT,
        STRING,
        CHAR
    }

    private record TokenMatch(Token token, int nextIndex) {
    }
}
