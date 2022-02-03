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

package com.heliosdecompiler.transformerapi.disassemblers.procyon;

import com.heliosdecompiler.transformerapi.TransformationException;
import com.heliosdecompiler.transformerapi.common.Loader;
import com.heliosdecompiler.transformerapi.common.ProcyonTask;
import com.heliosdecompiler.transformerapi.disassemblers.Disassembler;
import com.strobel.decompiler.DecompilerSettings;
import com.strobel.decompiler.languages.Language;
import com.strobel.decompiler.languages.Languages;

import java.io.IOException;

public class ProcyonDisassembler extends ProcyonTask implements Disassembler<DecompilerSettings> {

    @Override
    public String disassemble(String internalName, DecompilerSettings settings, Loader loader) throws TransformationException, IOException {
        return process(internalName, settings, loader);
    }

    @Override
    public Language language() {
        return Languages.bytecode();
    }
}
