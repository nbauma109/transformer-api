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

package com.heliosdecompiler.transformerapi.common.krakatau;

import com.heliosdecompiler.transformerapi.ExceptionalFunction;

import java.io.File;
import java.io.IOException;

public class KrakatauSettings {
    private File python2Exe;

    private ExceptionalFunction<ProcessBuilder, Process, IOException> processCreator;

    public KrakatauSettings setPythonExecutable(File location) {
        this.python2Exe = location;
        return this;
    }

    public KrakatauSettings setProcessCreator(ExceptionalFunction<ProcessBuilder, Process, IOException> creator) {
        this.processCreator = creator;
        return this;
    }

    public File getPython2Exe() {
        return python2Exe;
    }

    public ExceptionalFunction<ProcessBuilder, Process, IOException> getProcessCreator() {
        return processCreator;
    }
}
