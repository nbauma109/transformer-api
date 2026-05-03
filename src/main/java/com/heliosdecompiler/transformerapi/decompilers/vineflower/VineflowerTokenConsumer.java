/*
 * © 2025-2026 Nicolas Baumann (@nbauma109)
 * © 2025 sschr15 (@sschr15)
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
package com.heliosdecompiler.transformerapi.decompilers.vineflower;

import jd.core.DecompilationResult;
import jd.core.links.DeclarationData;
import jd.core.links.HyperlinkReferenceData;
import jd.core.links.ReferenceData;
import org.vineflower.java.decompiler.main.extern.TextTokenVisitor;
import org.vineflower.java.decompiler.struct.gen.FieldDescriptor;
import org.vineflower.java.decompiler.struct.gen.MethodDescriptor;
import org.vineflower.java.decompiler.util.token.TextRange;

public class VineflowerTokenConsumer extends TextTokenVisitor {
    private final DecompilationResult result;
    private String currentClass;

    public VineflowerTokenConsumer(DecompilationResult result, TextTokenVisitor next) {
        super(next);
        this.result = result;
    }

    @Override
    public void visitClass(TextRange range, boolean declaration, String name) {
        if (declaration) {
            currentClass = name;
            DeclarationData data = new DeclarationData(range.start, range.length, name, null, null);
            result.addDeclaration(toFragment(data, null), data);
            result.addTypeDeclaration(range.start, data);
        } else {
            ReferenceData reference = new ReferenceData(name, null, null, currentClass);
            addRef(range, reference);
        }
        super.visitClass(range, declaration, name);
    }

    @Override
    public void visitField(TextRange range, boolean declaration, String className, String name, FieldDescriptor descriptor) {
        if (declaration) {
            DeclarationData data = new DeclarationData(range.start, range.length, className, name, descriptor.descriptorString);
            result.addDeclaration(toFragment(data, descriptor.descriptorString), data);
        } else {
            ReferenceData reference = new ReferenceData(className, name, descriptor.descriptorString, currentClass);
            addRef(range, reference);
        }
        super.visitField(range, declaration, className, name, descriptor);
    }

    @Override
    public void visitMethod(TextRange range, boolean declaration, String className, String name, MethodDescriptor descriptor) {
        if (declaration) {
            DeclarationData data = new DeclarationData(range.start, range.length, className, name, descriptor.toString());
            result.addDeclaration(toFragment(data, descriptor.toString()), data);
        } else {
            ReferenceData reference = new ReferenceData(className, name, descriptor.toString(), currentClass);
            addRef(range, reference);
        }
        super.visitMethod(range, declaration, className, name, descriptor);
    }

    @Override
    public void visitLocal(TextRange range, boolean declaration, String className, String methodName, MethodDescriptor methodDescriptor, int index, String name) {
        visitMethodBound(range, declaration, className, methodName, methodDescriptor, index, false);
    }

    @Override
    public void visitParameter(TextRange range, boolean declaration, String className, String methodName, MethodDescriptor methodDescriptor, int index, String name) {
        visitMethodBound(range, declaration, className, methodName, methodDescriptor, index, true);
    }

    private void visitMethodBound(TextRange range, boolean declaration, String className, String methodName, MethodDescriptor methodDescriptor, int index, boolean isParameter) {
        String fakeDesc = methodDescriptor.toString() + '-' + (isParameter ? 'p' : 'l') + index;
        if (declaration) {
            DeclarationData data = new DeclarationData(range.start, range.length, className, methodName, fakeDesc);
            result.addDeclaration(toFragment(data, fakeDesc), data);
        } else {
            ReferenceData reference = new ReferenceData(className, methodName, fakeDesc, currentClass);
            addRef(range, reference);
        }
    }

    private void addRef(TextRange range, ReferenceData reference) {
        result.addReference(reference);
        reference.setEnabled(true);
        result.addHyperLink(range.start, new HyperlinkReferenceData(range.start, range.length, reference));
    }

    private static String toFragment(DeclarationData data, String desc) {
        if (data.isAType()) {
            return data.getTypeName();
        }
        return data.getTypeName() + '-' + data.getName() + '-' + desc;
    }
}
