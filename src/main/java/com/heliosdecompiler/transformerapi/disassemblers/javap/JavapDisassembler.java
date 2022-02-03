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

package com.heliosdecompiler.transformerapi.disassemblers.javap;

import com.heliosdecompiler.transformerapi.TransformationException;
import com.heliosdecompiler.transformerapi.common.Loader;
import com.heliosdecompiler.transformerapi.disassemblers.Disassembler;
import com.sun.tools.javap.JavapTask;
import com.sun.tools.javap.Options;

import java.io.IOException;
import java.io.StringWriter;

public class JavapDisassembler implements Disassembler<Options> {

    @Override
    public String disassemble(String internalName, Options settings, Loader loader) throws TransformationException, IOException {
        StringWriter stringWriter = new StringWriter();
        if (loader.canLoad(internalName)) {
            JavapTask task = new JavapTask(stringWriter, settings, internalName, loader.load(internalName));
            task.run();
        }
        return stringWriter.toString();
    }

    @Override
    public Options defaultSettings() {
        return new Options();
    }

}
