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

import org.apache.commons.lang3.time.StopWatch;

import com.heliosdecompiler.transformerapi.common.Loader;
import com.heliosdecompiler.transformerapi.decompilers.Decompiler;
import com.strobel.Procyon;
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
import java.lang.reflect.InvocationTargetException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jd.core.DecompilationResult;
import jd.core.links.ReferenceData;

public class ProcyonDecompiler extends Decompiler.AbstractDecompiler implements Decompiler<CommandLineOptions> {

    public ProcyonDecompiler(String name) {
        super(name);
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
        StopWatch stopWatch = StopWatch.createStarted();
        Map<String, byte[]> importantClasses = new HashMap<>();

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

        PlainTextOutput plainTextOutput = new ProcyonLinkProvider(stringwriter, stringwriter, result, internalName, referencesCache);
        TypeDecompilationResults typeDecompilationResults = com.strobel.decompiler.Decompiler.decompile(internalName, plainTextOutput, settings);
        if (options.getIncludeLineNumbers() || options.getStretchLines()) {
            List<LineNumberPosition> lineNumberPositions = typeDecompilationResults.getLineNumberPositions();
            EnumSet<LineNumberOption> lineNumberOptions = EnumSet.noneOf(LineNumberOption.class);

            if (options.getIncludeLineNumbers()) {
                lineNumberOptions.add(LineNumberOption.LEADING_COMMENTS);
            }

            if (options.getStretchLines()) {
                lineNumberOptions.add(LineNumberOption.STRETCHED);
            }

            InMemoryLineNumberFormatter lineFormatter = new InMemoryLineNumberFormatter(stringwriter.toString(), lineNumberPositions, lineNumberOptions);
            String sourceWithLineNumbers = lineFormatter.reformatFile();
            result.setDecompiledOutput(sourceWithLineNumbers);
        } else {
            result.setDecompiledOutput(stringwriter.toString());
        }
        time = stopWatch.getTime();
        return result;
    }


    @Override
    public long getDecompilationTime() {
        return time;
    }

    @Override
    public String getDecompilerVersion() {
        return Procyon.version();
    }

    @Override
    public CommandLineOptions defaultSettings() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        return new MapDecompilerSettings(MapDecompilerSettings.defaults());
    }

    @Override
    public CommandLineOptions lineNumberSettings()
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        return new MapDecompilerSettings(MapDecompilerSettings.lineNumbers());
    }

    @Override
    public boolean supportsRealignment() {
        return false;
    }
}
