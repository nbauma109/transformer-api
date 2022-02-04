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

package com.heliosdecompiler.transformerapi.decompilers.jd;

import org.jd.core.v1.ClassFileToJavaSourceDecompiler;
import org.jd.core.v1.printer.LineNumberStringBuilderPrinter;
import org.jd.core.v1.util.StringConstants;

import com.heliosdecompiler.transformerapi.TransformationException;
import com.heliosdecompiler.transformerapi.common.Loader;
import com.heliosdecompiler.transformerapi.decompilers.Decompiler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class JDCoreV1Decompiler implements Decompiler<Map<String, String>> {

    private static final ClassFileToJavaSourceDecompiler DECOMPILER = new ClassFileToJavaSourceDecompiler();

    @Override
    public String decompile(Loader loader, String internalName, Map<String, String> preferences) throws TransformationException, IOException {

        LineNumberStringBuilderPrinter printer = new LineNumberStringBuilderPrinter();

        return printer.buildDecompiledOutput(preferences, new JDLoader(loader), internalName + StringConstants.CLASS_FILE_SUFFIX, DECOMPILER);
    }

    @Override
    public Map<String, String> defaultSettings() {
        return new HashMap<>();
    }
}
