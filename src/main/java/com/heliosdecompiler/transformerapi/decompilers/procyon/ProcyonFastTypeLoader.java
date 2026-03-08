/*
 * Copyright 2022-2026 Nicolas Baumann (@nbauma109)
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

import com.heliosdecompiler.transformerapi.common.Loader;
import com.strobel.assembler.metadata.Buffer;
import com.strobel.assembler.metadata.ITypeLoader;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ProcyonFastTypeLoader implements ITypeLoader {

    private static final Logger log = LoggerFactory.getLogger(ProcyonFastTypeLoader.class);

    private final Map<String, byte[]> importantData = new HashMap<>();
    private final Loader loader;

    public ProcyonFastTypeLoader(Map<String, byte[]> importantClasses, Loader loader) {
        this.importantData.putAll(importantClasses);
        this.loader = loader;
    }

    @Override
    public boolean tryLoadType(String s, Buffer buffer) {
        if (importantData.containsKey(s)) {
            byte[] data = importantData.get(s);
            buffer.putByteArray(data, 0, data.length);
            buffer.position(0);
            return true;
        }
        if (loader.canLoad(s)) {
            try {
                byte[] data = loader.load(s);
                buffer.putByteArray(data, 0, data.length);
                buffer.position(0);
                return true;
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
        return false;
    }
}