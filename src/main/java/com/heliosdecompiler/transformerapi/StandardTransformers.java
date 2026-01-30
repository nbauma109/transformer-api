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
import com.heliosdecompiler.transformerapi.decompilers.Decompiler;
import com.heliosdecompiler.transformerapi.decompilers.cfr.CFRDecompiler;
import com.heliosdecompiler.transformerapi.decompilers.cfr.CFRSettings;
import com.heliosdecompiler.transformerapi.decompilers.fernflower.FernflowerDecompiler;
import com.heliosdecompiler.transformerapi.decompilers.fernflower.FernflowerSettings;
import com.heliosdecompiler.transformerapi.decompilers.jadx.JADXDecompiler;
import com.heliosdecompiler.transformerapi.decompilers.jadx.MapJadxArgs;
import com.heliosdecompiler.transformerapi.decompilers.jd.JDCoreV0Decompiler;
import com.heliosdecompiler.transformerapi.decompilers.jd.JDCoreV1Decompiler;
import com.heliosdecompiler.transformerapi.decompilers.procyon.MapDecompilerSettings;
import com.heliosdecompiler.transformerapi.decompilers.procyon.ProcyonDecompiler;
import com.heliosdecompiler.transformerapi.decompilers.vineflower.VineflowerDecompiler;
import com.heliosdecompiler.transformerapi.decompilers.vineflower.VineflowerSettings;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Set;

import jd.core.DecompilationResult;
import jd.core.preferences.Preferences;

public final class StandardTransformers {

    private StandardTransformers() {
    }

    public static DecompilationResult decompile(Loader apiLoader,
                                               String entryInternalName,
                                               Map<String, String> preferences,
                                               String engineName)
            throws IOException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        return Decompilers.decompile(apiLoader, entryInternalName, preferences, engineName);
    }

    public static final class Decompilers {

        private Decompilers() {
        }

        public static final String ENGINE_JD_CORE_V0 = "JD-Core v0";
        public static final String ENGINE_JD_CORE_V1 = "JD-Core v1";
        public static final String ENGINE_JD_CORE = "JD-Core";
        public static final String ENGINE_CFR = "CFR";
        public static final String ENGINE_PROCYON = "Procyon";
        public static final String ENGINE_FERNFLOWER = "Fernflower";
        public static final String ENGINE_VINEFLOWER = "Vineflower";
        public static final String ENGINE_JADX = "JADX";

        public static final Set<String> AVAILABLE_DECOMPILERS = Set.of(ENGINE_JD_CORE_V0, ENGINE_JD_CORE_V1, ENGINE_CFR, ENGINE_PROCYON, ENGINE_FERNFLOWER, ENGINE_VINEFLOWER, ENGINE_JADX);

        public static final ProcyonDecompiler PROCYON = new ProcyonDecompiler();
        public static final CFRDecompiler CFR = new CFRDecompiler();
        public static final FernflowerDecompiler FERNFLOWER = new FernflowerDecompiler();
        public static final VineflowerDecompiler VINEFLOWER = new VineflowerDecompiler();
        public static final JDCoreV0Decompiler JD_CORE_V0 = new JDCoreV0Decompiler();
        public static final JDCoreV1Decompiler JD_CORE_V1 = new JDCoreV1Decompiler();
        public static final JADXDecompiler JADX = new JADXDecompiler();

        public static Decompiler<?> valueOf(String engineName) throws IllegalArgumentException {
            return switch (engineName) {
                case ENGINE_JD_CORE_V0 -> JD_CORE_V0;
                case ENGINE_JD_CORE, ENGINE_JD_CORE_V1 -> JD_CORE_V1;
                case ENGINE_CFR -> CFR;
                case ENGINE_FERNFLOWER -> FERNFLOWER;
                case ENGINE_PROCYON -> PROCYON;
                case ENGINE_JADX -> JADX;
                default -> VINEFLOWER;
            };
        }

        public static DecompilationResult decompile(Loader apiLoader,
                String entryInternalName,
                Map<String, String> preferences,
                String engineName)
                        throws IOException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            return switch (engineName) {
                case ENGINE_JD_CORE_V0 -> JD_CORE_V0.decompile(apiLoader, entryInternalName, new Preferences(preferences));
                case ENGINE_JD_CORE, ENGINE_JD_CORE_V1 -> JD_CORE_V1.decompile(apiLoader, entryInternalName, preferences);
                case ENGINE_CFR -> CFR.decompile(apiLoader, entryInternalName, new CFRSettings(preferences));
                case ENGINE_FERNFLOWER -> FERNFLOWER.decompile(apiLoader, entryInternalName, new FernflowerSettings(preferences));
                case ENGINE_PROCYON -> PROCYON.decompile(apiLoader, entryInternalName, new MapDecompilerSettings(preferences));
                case ENGINE_JADX -> JADX.decompile(apiLoader, entryInternalName, new MapJadxArgs(preferences));
                default -> VINEFLOWER.decompile(apiLoader, entryInternalName, new VineflowerSettings(preferences));
            };
        }
    }
}
