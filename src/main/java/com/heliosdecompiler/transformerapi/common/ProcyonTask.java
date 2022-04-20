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

import com.heliosdecompiler.transformerapi.decompilers.procyon.ProcyonFastTypeLoader;
import com.strobel.Procyon;
import com.strobel.assembler.metadata.FieldDefinition;
import com.strobel.assembler.metadata.FieldReference;
import com.strobel.assembler.metadata.MethodDefinition;
import com.strobel.assembler.metadata.MethodReference;
import com.strobel.assembler.metadata.TypeDefinition;
import com.strobel.assembler.metadata.TypeReference;
import com.strobel.decompiler.DecompilerSettings;
import com.strobel.decompiler.PlainTextOutput;
import com.strobel.decompiler.languages.Language;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import jd.core.DecompilationResult;
import jd.core.links.DeclarationData;
import jd.core.links.HyperlinkReferenceData;
import jd.core.links.ReferenceData;

public abstract class ProcyonTask {

    protected abstract Language language();

    public DecompilerSettings defaultSettings() {
        DecompilerSettings decompilerSettings = new DecompilerSettings();
        decompilerSettings.setLanguage(language());
        decompilerSettings.setForceExplicitImports(true);
        decompilerSettings.setFlattenSwitchBlocks(true);
        decompilerSettings.setRetainRedundantCasts(true);
        decompilerSettings.setOutputFileHeaderText("\nDecompiled by Procyon v" + Procyon.version() + "\n");
        return decompilerSettings;
    }

    protected DecompilationResult process(Loader loader, String internalName, DecompilerSettings settings) throws IOException {
        Map<String, byte[]> importantClasses = new HashMap<>();
        importantClasses.put(internalName, loader.load(internalName));
        if (settings.getTypeLoader() == null) {
            settings.setTypeLoader(new ProcyonFastTypeLoader(importantClasses, loader));
        }
        StringWriter stringwriter = new StringWriter();
        DecompilationResult result = new DecompilationResult();
        Map<String, ReferenceData> referencesCache = new HashMap<>();

        PlainTextOutput plainTextOutput = new PlainTextOutput(stringwriter) {
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
                            result.addDeclaration(internalTypeName + '-' + name + '-' + descriptor, new DeclarationData(from, text.length(), internalTypeName, name, descriptor));
                        } else if (definition instanceof FieldDefinition) {
                            FieldDefinition field = (FieldDefinition) definition;
                            String descriptor = field.getErasedSignature();
                            TypeReference type = field.getDeclaringType();
                            String internalTypeName = type.getInternalName();
                            String name = field.getName();
                            result.addDeclaration(internalTypeName + '-' + name + '-' + descriptor, new DeclarationData(from, text.length(), internalTypeName, name, descriptor));
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
        com.strobel.decompiler.Decompiler.decompile(internalName, plainTextOutput, settings);
        result.setDecompiledOutput(stringwriter.toString());
        return result;
    }

}
