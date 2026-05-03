/*
 * © 2026 Nicolas Baumann (@nbauma109)
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.strobel.assembler.metadata.FieldDefinition;
import com.strobel.assembler.metadata.FieldReference;
import com.strobel.assembler.metadata.MethodDefinition;
import com.strobel.assembler.metadata.MethodReference;
import com.strobel.assembler.metadata.TypeDefinition;
import com.strobel.assembler.metadata.TypeReference;
import com.strobel.decompiler.PlainTextOutput;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

import jd.core.DecompilationResult;
import jd.core.links.DeclarationData;
import jd.core.links.HyperlinkReferenceData;
import jd.core.links.ReferenceData;
import jd.core.links.StringData;

public final class ProcyonLinkProvider extends PlainTextOutput {

    private static final Logger log = LoggerFactory.getLogger(ProcyonLinkProvider.class);

    private final StringWriter stringwriter;
    private final DecompilationResult result;
    private final String internalName;
    private final Map<String, ReferenceData> referencesCache;

    ProcyonLinkProvider(Writer writer, StringWriter stringwriter, DecompilationResult result,
            String internalName, Map<String, ReferenceData> referencesCache) {
        super(writer);
        this.stringwriter = stringwriter;
        this.result = result;
        this.internalName = internalName;
        this.referencesCache = referencesCache;
    }

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
                if (definition instanceof TypeDefinition type) {
                    String internalTypeName = type.getInternalName();
                    DeclarationData data = new DeclarationData(from, text.length(), internalTypeName, null, null);
                    result.addDeclaration(internalTypeName, data);
                    result.addTypeDeclaration(from, data);
                } else if (definition instanceof MethodDefinition method) {
                    String descriptor = method.getErasedSignature();
                    TypeReference type = method.getDeclaringType();
                    String internalTypeName = type.getInternalName();
                    String name = method.getName();
                    addDeclaration(text, from, descriptor, internalTypeName, name);
                } else if (definition instanceof FieldDefinition field) {
                    String descriptor = field.getErasedSignature();
                    TypeReference type = field.getDeclaringType();
                    String internalTypeName = type.getInternalName();
                    String name = field.getName();
                    addDeclaration(text, from, descriptor, internalTypeName, name);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public void writeReference(String text, Object reference, boolean isLocal) {
        super.writeReference(text, reference, isLocal);
        try {
            if (text != null && reference != null) {
                int from = stringwriter.getBuffer().length() - text.length();
                if (reference instanceof TypeReference type) {
                    String internalTypeName = type.getInternalName();
                    ReferenceData data = newReferenceData(internalTypeName, null, null, internalName);
                    result.addHyperLink(from, new HyperlinkReferenceData(from, text.length(), data));
                } else if (reference instanceof MethodReference method) {
                    String descriptor = method.getErasedSignature();
                    TypeReference type = method.getDeclaringType();
                    String internalTypeName = type.getInternalName();
                    String name = method.getName();
                    ReferenceData data = newReferenceData(internalTypeName, name, descriptor, internalName);
                    result.addHyperLink(from, new HyperlinkReferenceData(from, text.length(), data));
                } else if (reference instanceof FieldReference field) {
                    String descriptor = field.getErasedSignature();
                    TypeReference type = field.getDeclaringType();
                    String internalTypeName = type.getInternalName();
                    String name = field.getName();
                    ReferenceData data = newReferenceData(internalTypeName, name, descriptor, internalName);
                    result.addHyperLink(from, new HyperlinkReferenceData(from, text.length(), data));
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
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
}