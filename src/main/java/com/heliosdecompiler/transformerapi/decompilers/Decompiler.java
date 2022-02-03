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

package com.heliosdecompiler.transformerapi.decompilers;

import com.heliosdecompiler.transformerapi.TransformationException;
import com.heliosdecompiler.transformerapi.common.Loader;

import java.io.IOException;

/**
 * Represents a particular Decompiler.
 * Note that decompiler implementations should be stateless, and thus can be reused (and are thread safe)
 */
public interface Decompiler<S> {

    /**
     * Decompile the given class with a loader
     * 
     * @param loader   The loader implementation used to load the class
     * @param internalName The internal name of the class to decompile
     * @param settings The settings to use with this decompiler
     * @return The decompiled class
     * @throws TransformationException
     * @throws IOException 
     */
    String decompile(Loader loader, String internalName, S settings) throws TransformationException, IOException;
    
    S defaultSettings();
    
    default String decompile(Loader loader, String internalName) throws TransformationException, IOException {
        return decompile(loader, internalName, defaultSettings());
    }
}
