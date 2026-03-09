/*
 * Copyright 2022-2026 Nicolas Baumann (@nbauma109)
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
 */
package com.heliosdecompiler.transformerapi.decompilers.jadx;

import org.apache.commons.lang3.time.StopWatch;

import com.heliosdecompiler.transformerapi.common.BytecodeSourceLinker;
import com.heliosdecompiler.transformerapi.common.Loader;
import com.heliosdecompiler.transformerapi.common.ResultLinkSupport;
import com.heliosdecompiler.transformerapi.decompilers.Decompiler;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jadx.api.ICodeInfo;
import jadx.api.JadxArgs;
import jadx.api.JadxDecompiler;
import jadx.api.JavaClass;
import jadx.api.JavaField;
import jadx.api.JavaMethod;
import jadx.api.JavaNode;
import jadx.api.JavaVariable;
import jadx.core.Jadx;
import jadx.core.codegen.TypeGen;
import jadx.core.utils.Utils;
import jadx.plugins.input.java.JavaClassReader;
import jadx.plugins.input.java.JavaLoadResult;
import jd.core.DecompilationResult;
import jd.core.links.ReferenceData;
import jadx.api.metadata.ICodeAnnotation;
import jadx.api.metadata.ICodeNodeRef;
import jadx.api.metadata.annotations.NodeDeclareRef;

/**
 * Provides a gateway to the JADX decompiler.
 */
public class JADXDecompiler extends Decompiler.AbstractDecompiler implements Decompiler<JadxArgs> {

    public JADXDecompiler(String name) {
        super(name);
    }

    @Override
    public DecompilationResult decompile(Loader loader, String internalName, JadxArgs args) throws IOException {
        StopWatch stopWatch = StopWatch.createStarted();

        DecompilationResult decompilationResult = new DecompilationResult();
        Map<String, byte[]> importantData = readClassAndInnerClasses(loader, internalName).importantData();
        if (!importantData.isEmpty()) {
            int i = 0;
            List<JavaClassReader> readers = new ArrayList<>();
            for (Map.Entry<String, byte[]> ent : importantData.entrySet()) {
                readers.add(new JavaClassReader(i++, ent.getKey(), ent.getValue()));
            }
            try (JadxDecompiler jadx = new JadxDecompiler(args); JavaLoadResult javaLoadResult = new JavaLoadResult(readers, null)) {
                jadx.addCustomCodeLoader(javaLoadResult);
                jadx.load();
                for (JavaClass cls : jadx.getClasses()) {
                    if (cls.getClassNode().getClsData().getInputFileName().equals(internalName)) {
                        ICodeInfo codeInfo = cls.getCodeInfo();
                        decompilationResult.setDecompiledOutput(codeInfo.getCodeStr());
                        addLinks(decompilationResult, jadx, codeInfo);
                        // Supplement JADX's native annotations with bytecode-driven links for missed constructor cases.
                        BytecodeSourceLinker.link(decompilationResult, codeInfo.getCodeStr(), internalName, importantData);
                        Map<Integer, Integer> lineMapping = codeInfo.getCodeMetadata().getLineMapping();
                        if (!Utils.isEmpty(lineMapping)) {
                            putLineNumbers(decompilationResult, lineMapping);
                        } else {
                            putIdentityLineNumbers(decompilationResult, codeInfo.getCodeStr());
                        }
                        break;
                    }
                }
            }
        }

        time = stopWatch.getTime();

        return decompilationResult;
    }

    @Override
    public long getDecompilationTime() {
        return time;
    }

    @Override
    public String getDecompilerVersion() {
        return Jadx.getVersion();
    }

    @Override
    public JadxArgs defaultSettings() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        return new MapJadxArgs(new HashMap<>());
    }

    @Override
    public JadxArgs lineNumberSettings() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        return new MapJadxArgs(MapJadxArgs.lineNumbers());
    }

    @Override
    public boolean supportsRealignment() {
        return false;
    }

    private static void addLinks(DecompilationResult result, JadxDecompiler jadx, ICodeInfo codeInfo) {
        String code = codeInfo.getCodeStr();
        Map<String, ReferenceData> references = new HashMap<>();
        /* JADX already exposes source positions, so one pass is enough to rebuild the link model. */
        for (Map.Entry<Integer, ICodeAnnotation> entry : codeInfo.getCodeMetadata().getAsMap().entrySet()) {
            addLinkAtPosition(result, jadx, codeInfo, code, references, entry.getKey(), entry.getValue());
        }
    }

    private static void addLinkAtPosition(DecompilationResult result, JadxDecompiler jadx, ICodeInfo codeInfo, String code, Map<String, ReferenceData> references, int position, ICodeAnnotation annotation) {
        if (annotation instanceof NodeDeclareRef declaration) {
            addDeclaration(result, jadx, declaration.getNode(), position, code);
            return;
        }
        if (isReferenceAnnotation(annotation)) {
            addReference(result, jadx, codeInfo, code, references, position, annotation);
        }
    }

    private static boolean isReferenceAnnotation(ICodeAnnotation annotation) {
        return switch (annotation.getAnnType()) {
            case CLASS, METHOD, FIELD, VAR, VAR_REF -> true;
            default -> false;
        };
    }

    private static void addDeclaration(DecompilationResult result, JadxDecompiler jadx, ICodeNodeRef node, int position, String code) {
        JavaNode javaNode = jadx.getJavaNodeByRef(node);
        addDeclaration(result, javaNode, position, code);
    }

    private static void addDeclaration(DecompilationResult result, JavaNode javaNode, int position, String code) {
        if (javaNode == null) {
            return;
        }
        ResultLinkSupport.LinkTarget target = toTarget(javaNode);
        if (target == null) {
            return;
        }
        int length = ResultLinkSupport.identifierLength(code, position);
        if (length <= 0) {
            return;
        }
        ResultLinkSupport.addDeclaration(result, position, length, target);
    }

    private static void addReference(DecompilationResult result, JadxDecompiler jadx, ICodeInfo codeInfo, String code, Map<String, ReferenceData> references, int position, ICodeAnnotation annotation) {
        JavaNode javaNode = jadx.getJavaNodeByCodeAnnotation(codeInfo, annotation);
        if (javaNode == null) {
            return;
        }
        if (position == javaNode.getDefPos()) {
            addDeclaration(result, javaNode, position, code);
            return;
        }
        int length = ResultLinkSupport.identifierLength(code, position);
        if (length <= 0) {
            return;
        }
        ResultLinkSupport.LinkTarget target = toTarget(javaNode);
        if (target != null) {
            String scope = enclosingType(jadx, codeInfo, position);
            ResultLinkSupport.addReference(result, references, position, length, target, scope);
        }
    }

    private static String enclosingType(JadxDecompiler jadx, ICodeInfo codeInfo, int position) {
        JavaNode enclosing = jadx.getEnclosingNode(codeInfo, position);
        if (enclosing instanceof JavaClass javaClass) {
            return internalName(javaClass.getClassNode().getClassInfo().getRawName());
        }
        if (enclosing instanceof JavaMethod javaMethod) {
            return internalName(javaMethod.getDeclaringClass().getClassNode().getClassInfo().getRawName());
        }
        return null;
    }

    private static ResultLinkSupport.LinkTarget toTarget(JavaNode node) {
        if (node instanceof JavaClass javaClass) {
            return new ResultLinkSupport.LinkTarget(internalName(javaClass.getClassNode().getClassInfo().getRawName()), null, null);
        }
        if (node instanceof JavaMethod javaMethod) {
            String owner = internalName(javaMethod.getDeclaringClass().getClassNode().getClassInfo().getRawName());
            String name = javaMethod.getMethodNode().getMethodInfo().getName();
            String descriptor = methodDescriptor(javaMethod);
            return new ResultLinkSupport.LinkTarget(owner, name, descriptor);
        }
        if (node instanceof JavaField javaField) {
            String owner = internalName(javaField.getDeclaringClass().getClassNode().getClassInfo().getRawName());
            String name = javaField.getFieldNode().getFieldInfo().getName();
            String descriptor = TypeGen.signature(javaField.getFieldNode().getFieldInfo().getType());
            return new ResultLinkSupport.LinkTarget(owner, name, descriptor);
        }
        if (node instanceof JavaVariable javaVariable) {
            String owner = internalName(javaVariable.getDeclaringClass().getClassNode().getClassInfo().getRawName());
            String name = javaVariable.getMth().getMethodNode().getMethodInfo().getName();
            String descriptor = javaVariable.getMth().getMethodNode().getMethodInfo().getShortId()
                + "-v" + javaVariable.getReg() + '-' + javaVariable.getSsa();
            return new ResultLinkSupport.LinkTarget(owner, name, descriptor);
        }
        return null;
    }

    private static String internalName(String rawName) {
        return rawName.replace('.', '/');
    }

    private static String methodDescriptor(JavaMethod javaMethod) {
        // Jadx short ids are `<name><descriptor>`, so the JVM descriptor starts right after the name.
        String name = javaMethod.getMethodNode().getMethodInfo().getName();
        String shortId = javaMethod.getMethodNode().getMethodInfo().getShortId();
        return shortId.substring(name.length());
    }

}
