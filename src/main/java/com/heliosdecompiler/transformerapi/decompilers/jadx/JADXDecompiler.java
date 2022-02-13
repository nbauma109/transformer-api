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
package com.heliosdecompiler.transformerapi.decompilers.jadx;

import com.heliosdecompiler.transformerapi.TransformationException;
import com.heliosdecompiler.transformerapi.common.Loader;
import com.heliosdecompiler.transformerapi.decompilers.Decompiler;

import java.io.IOException;
import java.util.Map;

import jadx.api.CommentsLevel;
import jadx.api.JadxArgs;
import jadx.api.JadxDecompiler;
import jadx.api.JavaClass;

public class JADXDecompiler implements Decompiler<JadxArgs> {

    @Override
    public String decompile(Loader loader, String internalName, JadxArgs args) throws TransformationException, IOException {
        Map<String, byte[]> importantData = readClassAndInnerClasses(loader, internalName);
        if (!importantData.isEmpty()) {
            for (Map.Entry<String, byte[]> ent : importantData.entrySet()) {
                args.getInputFiles().add(new JADXInMemoryFile(ent.getKey(), ent.getValue()));
            }
            try (JadxDecompiler jadx = new JadxDecompiler(args)) {
                jadx.load();
                for (JavaClass cls : jadx.getClasses()) {
                    if (cls.getClassNode().getClsData().getInputFileName().equals(internalName)) {
                        return cls.getCode();
                    }
                }
            }
        }
        return null;
    }

    @Override
    public JadxArgs defaultSettings() {
        JadxArgs jadxArgs = new JadxArgs();
        jadxArgs.setCommentsLevel(CommentsLevel.WARN);
        return jadxArgs;
    }

}
