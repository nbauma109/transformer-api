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

package com.heliosdecompiler.transformerapi;

import com.heliosdecompiler.transformerapi.common.Loader;
import com.heliosdecompiler.transformerapi.decompilers.cfr.CFRDecompiler;
import com.heliosdecompiler.transformerapi.decompilers.fernflower.FernflowerDecompiler;
import com.heliosdecompiler.transformerapi.decompilers.jadx.JADXDecompiler;
import com.heliosdecompiler.transformerapi.decompilers.jd.JDCoreV0Decompiler;
import com.heliosdecompiler.transformerapi.decompilers.jd.JDCoreV1Decompiler;
import com.heliosdecompiler.transformerapi.decompilers.procyon.ProcyonDecompiler;
import com.heliosdecompiler.transformerapi.disassemblers.procyon.ProcyonDisassembler;

import java.io.IOException;
import java.util.Map;

import jd.core.DecompilationResult;
import jd.core.preferences.Preferences;

public final class StandardTransformers {

    private StandardTransformers() {
    }

    public static DecompilationResult decompile(Loader apiLoader, String entryInternalName, Map<String, String> preferences, String engineName) throws TransformationException, IOException {
        if (engineName.endsWith("Disassembler")) {
            return Disassemblers.disassemble(apiLoader, entryInternalName, engineName);
        }
        return Decompilers.decompile(apiLoader, entryInternalName, preferences, engineName);
    }

    public static final class Decompilers {

        private Decompilers() {
        }

        public static final String ENGINE_JD_CORE_V0 = "JD-Core v0";
        public static final String ENGINE_JD_CORE_V1 = "JD-Core v1";
        public static final String ENGINE_CFR = "CFR";
        public static final String ENGINE_PROCYON = "Procyon";
        public static final String ENGINE_FERNFLOWER = "Fernflower";
        public static final String ENGINE_JADX = "JADX";

        public static final ProcyonDecompiler PROCYON = new ProcyonDecompiler();
        public static final CFRDecompiler CFR = new CFRDecompiler();
        public static final FernflowerDecompiler FERNFLOWER = new FernflowerDecompiler();
        public static final JDCoreV0Decompiler JD_CORE_V0 = new JDCoreV0Decompiler();
        public static final JDCoreV1Decompiler JD_CORE_V1 = new JDCoreV1Decompiler();
        public static final JADXDecompiler JADX = new JADXDecompiler();

        public static DecompilationResult decompile(Loader apiLoader, String entryInternalName, Map<String, String> preferences, String engineName) throws TransformationException, IOException {
            return switch (engineName) {
                case ENGINE_JD_CORE_V0 -> JD_CORE_V0.decompile(apiLoader, entryInternalName, new Preferences(preferences));
                case ENGINE_JD_CORE_V1 -> JD_CORE_V1.decompile(apiLoader, entryInternalName, preferences);
                case ENGINE_CFR -> CFR.decompile(apiLoader, entryInternalName);
                case ENGINE_FERNFLOWER -> FERNFLOWER.decompile(apiLoader, entryInternalName);
                case ENGINE_PROCYON -> PROCYON.decompile(apiLoader, entryInternalName);
                case ENGINE_JADX -> JADX.decompile(apiLoader, entryInternalName);
                default -> throw new IllegalArgumentException("Unexpected decompiler engine: " + engineName);
            };
        }
    }

    public static final class Disassemblers {

        private Disassemblers() {
        }

        public static final String ENGINE_PROCYON_DISASSEMBLER = "Procyon Disassembler";

        public static DecompilationResult disassemble(Loader apiLoader, String entryInternalName, String engineName) throws TransformationException, IOException {
            return switch (engineName) {
                case ENGINE_PROCYON_DISASSEMBLER -> PROCYON.disassemble(apiLoader, entryInternalName);
                default -> throw new IllegalArgumentException("Unexpected disassembler engine: " + engineName);
            };
        }

        public static final ProcyonDisassembler PROCYON = new ProcyonDisassembler();
    }

}
