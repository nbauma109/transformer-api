/*
 * Copyright 2017 Sam Sun <github-contact@samczsun.com>
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

package com.heliosdecompiler.transformerapi.decompilers.procyon;

import com.heliosdecompiler.transformerapi.common.Loader;
import com.heliosdecompiler.transformerapi.decompilers.Decompiler;
import com.strobel.Procyon;
import com.strobel.assembler.metadata.FieldDefinition;
import com.strobel.assembler.metadata.FieldReference;
import com.strobel.assembler.metadata.MethodDefinition;
import com.strobel.assembler.metadata.MethodReference;
import com.strobel.assembler.metadata.TypeDefinition;
import com.strobel.assembler.metadata.TypeReference;
import com.strobel.decompiler.CommandLineOptions;
import com.strobel.decompiler.DecompilerSettings;
import com.strobel.decompiler.InMemoryLineNumberFormatter;
import com.strobel.decompiler.LineNumberOption;
import com.strobel.decompiler.PlainTextOutput;
import com.strobel.decompiler.languages.BytecodeOutputOptions;
import com.strobel.decompiler.languages.Languages;
import com.strobel.decompiler.languages.LineNumberPosition;
import com.strobel.decompiler.languages.TypeDecompilationResults;

import java.io.IOException;
import java.io.StringWriter;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jd.core.DecompilationResult;
import jd.core.links.DeclarationData;
import jd.core.links.HyperlinkReferenceData;
import jd.core.links.ReferenceData;
import jd.core.links.StringData;

public class ProcyonDecompiler implements Decompiler<CommandLineOptions> {

    public CommandLineOptions defaultSettings() {
        CommandLineOptions options = new CommandLineOptions();
        options.setCollapseImports(false);
        options.setFlattenSwitchBlocks(true);
        options.setRetainRedundantCasts(true);
        options.setSuppressBanner(false);
        return options;
    }

    private static BytecodeOutputOptions createBytecodeFormattingOptions(final CommandLineOptions options) {
        if (options.isVerbose()) {
            return BytecodeOutputOptions.createVerbose();
        }

        final BytecodeOutputOptions bytecodeOptions = BytecodeOutputOptions.createDefault();

        bytecodeOptions.showTypeAttributes = options.getShowTypeAttributes();
        bytecodeOptions.showConstantPool = options.getShowConstantPool();
        bytecodeOptions.showLineNumbers = options.getIncludeLineNumbers();
        bytecodeOptions.showLocalVariableTables = options.getShowLocalVariableDetails();
        bytecodeOptions.showMethodsStack = options.getShowLocalVariableDetails();

        return bytecodeOptions;
    }

    @Override
    public DecompilationResult decompile(Loader loader, String internalName, CommandLineOptions options) throws IOException {
        Map<String, byte[]> importantClasses = new HashMap<>();
        importantClasses.put(internalName, loader.load(internalName));

        final DecompilerSettings settings = new DecompilerSettings();

        settings.setFlattenSwitchBlocks(options.getFlattenSwitchBlocks());
        settings.setForceExplicitImports(!options.getCollapseImports());
        settings.setForceExplicitTypeArguments(options.getForceExplicitTypeArguments());
        settings.setRetainRedundantCasts(options.getRetainRedundantCasts());
        settings.setShowSyntheticMembers(options.getShowSyntheticMembers());
        settings.setExcludeNestedTypes(options.getExcludeNestedTypes());
        settings.setOutputDirectory(options.getOutputDirectory());
        settings.setIncludeLineNumbersInBytecode(options.getIncludeLineNumbers());
        settings.setRetainPointlessSwitches(options.getRetainPointlessSwitches());
        settings.setUnicodeOutputEnabled(options.isUnicodeOutputEnabled());
        settings.setMergeVariables(options.getMergeVariables());
        settings.setShowDebugLineNumbers(options.getShowDebugLineNumbers());
        settings.setSimplifyMemberReferences(options.getSimplifyMemberReferences());
        settings.setForceFullyQualifiedReferences(options.getForceFullyQualifiedReferences());
        settings.setDisableForEachTransforms(options.getDisableForEachTransforms());
        settings.setForcedCompilerTarget(options.getCompilerTargetOverride());
        settings.setTextBlockLineMinimum(options.getTextBlockLineMinimum());
        settings.setTypeLoader(new ProcyonFastTypeLoader(importantClasses, loader));

        if (!options.getSuppressBanner()) {
            settings.setOutputFileHeaderText("\nDecompiled by Procyon v" + Procyon.version() + "\n");
        }

        if (options.isRawBytecode()) {
            settings.setLanguage(Languages.bytecode());
            settings.setBytecodeOutputOptions(createBytecodeFormattingOptions(options));
        } else if (options.isBytecodeAst()) {
            settings.setLanguage(options.isUnoptimized() ? Languages.bytecodeAstUnoptimized() : Languages.bytecodeAst());
        }

        StringWriter stringwriter = new StringWriter();
        DecompilationResult result = new DecompilationResult();
        Map<String, ReferenceData> referencesCache = new HashMap<>();

        PlainTextOutput plainTextOutput = (options.getIncludeLineNumbers() || options.getStretchLines()) ? new PlainTextOutput(stringwriter)
                                                                                                         : new PlainTextOutput(stringwriter) {

            private void addDeclaration(String text, int from, String descriptor, String internalTypeName, String name) {
                String key = internalTypeName + '-' + name + '-' + descriptor;
                result.addDeclaration(key, new DeclarationData(from, text.length(), internalTypeName, name, descriptor));
            }

            @Override
            public void writeDefinition(String text, Object definition, boolean isLocal) {
                super.writeDefinition(text, definition, isLocal);
                try {
                    if (text != null && definition != null) {
                        int from = stringwriter.getBuffer().length() - text.length();
                        if (definition instanceof TypeDefinition) {
                            TypeDefinition type = (TypeDefinition) definition;
                            String internalTypeName = type.getInternalName();
                            DeclarationData data = new DeclarationData(from, text.length(), internalTypeName, null, null);
                            result.addDeclaration(internalTypeName, data);
                            result.addTypeDeclaration(from, data);
                        } else if (definition instanceof MethodDefinition) {
                            MethodDefinition method = (MethodDefinition) definition;
                            String descriptor = method.getErasedSignature();
                            TypeReference type = method.getDeclaringType();
                            String internalTypeName = type.getInternalName();
                            String name = method.getName();
                            addDeclaration(text, from, descriptor, internalTypeName, name);
                        } else if (definition instanceof FieldDefinition) {
                            FieldDefinition field = (FieldDefinition) definition;
                            String descriptor = field.getErasedSignature();
                            TypeReference type = field.getDeclaringType();
                            String internalTypeName = type.getInternalName();
                            String name = field.getName();
                            addDeclaration(text, from, descriptor, internalTypeName, name);
                        }
                    }
                } catch (Exception e) {
                    System.err.println(e);
                }
            }

            @Override
            public void writeReference(String text, Object reference, boolean isLocal) {
                super.writeReference(text, reference, isLocal);
                try {
                    if (text != null && reference != null) {
                        int from = stringwriter.getBuffer().length() - text.length();
                        if (reference instanceof TypeReference) {
                            TypeReference type = (TypeReference) reference;
                            String internalTypeName = type.getInternalName();
                            ReferenceData data = newReferenceData(internalTypeName, null, null, internalName);
                            result.addHyperLink(from, new HyperlinkReferenceData(from, text.length(), data));
                        } else if (reference instanceof MethodReference) {
                            MethodReference method = (MethodReference) reference;
                            String descriptor = method.getErasedSignature();
                            TypeReference type = method.getDeclaringType();
                            String internalTypeName = type.getInternalName();
                            String name = method.getName();
                            ReferenceData data = newReferenceData(internalTypeName, name, descriptor, internalName);
                            result.addHyperLink(from, new HyperlinkReferenceData(from, text.length(), data));
                        } else if (reference instanceof FieldReference) {
                            FieldReference field = (FieldReference) reference;
                            String descriptor = field.getErasedSignature();
                            TypeReference type = field.getDeclaringType();
                            String internalTypeName = type.getInternalName();
                            String name = field.getName();
                            ReferenceData data = newReferenceData(internalTypeName, name, descriptor, internalName);
                            result.addHyperLink(from, new HyperlinkReferenceData(from, text.length(), data));
                        }
                    }
                } catch (Exception e) {
                    System.err.println(e);
                }
            }

            @Override
            public void writeTextLiteral(Object value) {
                super.writeTextLiteral(value);
                String text = value.toString();
                int from = stringwriter.getBuffer().length() - text.length();
                result.addString(new StringData(from, text, internalName));
            }

            // --- Add references --- //
            public ReferenceData newReferenceData(String internalName, String name, String descriptor, String scopeInternalName) {
                String key = internalName + '-' + name + '-' + descriptor + '-' + scopeInternalName;
                return referencesCache.computeIfAbsent(key, k -> {
                    ReferenceData reference = new ReferenceData(internalName, name, descriptor, scopeInternalName);
                    result.addReference(reference);
                    return reference;
                });
            }
        };
        final TypeDecompilationResults results = com.strobel.decompiler.Decompiler.decompile(internalName, plainTextOutput, settings);
        final List<LineNumberPosition> lineNumberPositions = results.getLineNumberPositions();
        for (LineNumberPosition lineNumberPosition : lineNumberPositions) {
            result.getLineNumbers().put(lineNumberPosition.getEmittedLine(), lineNumberPosition.getOriginalLine());
        }
        if (options.getIncludeLineNumbers() || options.getStretchLines()) {
            final EnumSet<LineNumberOption> lineNumberOptions = EnumSet.noneOf(LineNumberOption.class);

            if (options.getIncludeLineNumbers()) {
                lineNumberOptions.add(LineNumberOption.LEADING_COMMENTS);
            }

            if (options.getStretchLines()) {
                lineNumberOptions.add(LineNumberOption.STRETCHED);
            }

            final InMemoryLineNumberFormatter lineFormatter = new InMemoryLineNumberFormatter(
                plainTextOutput.toString(),
                lineNumberPositions,
                lineNumberOptions
            );

            String reformattedOutput = lineFormatter.reformatFile();
            result.setDecompiledOutput(reformattedOutput);
        } else {
            result.setDecompiledOutput(stringwriter.toString());
        }
        return result;
    }
}
