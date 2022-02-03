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

package com.heliosdecompiler.transformerapi.disassemblers;

import com.heliosdecompiler.transformerapi.TransformationException;
import com.heliosdecompiler.transformerapi.common.Loader;

import java.io.IOException;

/**
 * Represents a particular Disassembler.
 * Note that disassembler implementations should be stateless, and thus can be reused (and are thread safe)
 */
public interface Disassembler<S> {
    
    /**
     * Disassemble the given class with a loader
     * 
     * @param internalName The internal name of the class to disassemble
     * @param settings The settings to use with this disassembler
     * @param loader   The loader implementation used to load the class
     * @return The disassembled class
     * @throws TransformationException
     * @throws IOException 
     */
    String disassemble(String internalName, S settings, Loader loader) throws TransformationException, IOException;
    

    S defaultSettings();
}
