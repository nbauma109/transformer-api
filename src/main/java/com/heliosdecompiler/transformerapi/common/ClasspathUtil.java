/*
 * © 2022 Nicolas Baumann (@nbauma109)
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
package com.heliosdecompiler.transformerapi.common;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public final class ClasspathUtil {

    private ClasspathUtil() {
    }

    public static List<String> getJDKClasspath() {
        List<String> cpEntries = new ArrayList<>();
        String javaHome = System.getenv("JAVA_HOME");
        if (javaHome != null && !javaHome.isBlank()) {
            File jmods = new File(javaHome, "jmods");
            if (jmods.exists() && jmods.isDirectory()) {
                File[] files = jmods.listFiles();
                for (File file : files) {
                    cpEntries.add(file.getAbsolutePath());
                }
            }
            File rt = new File(javaHome, "jre/lib/rt.jar");
            if (rt.exists() && rt.isFile()) {
                cpEntries.add(rt.getAbsolutePath());
            }
        }
        return cpEntries;
    }

    public static String[] createClasspathEntries(URI jarURI, List<String> jdkClasspath) {
        List<String> cpEntries = new ArrayList<>(jdkClasspath);
        File parentFile = new File(jarURI).getParentFile();
        if (parentFile.isDirectory()) {
            File[] files = parentFile.listFiles((dir, name) -> new File(dir, name).isFile() && name.matches(".*(?<!-sources)\\.jar$"));
            if (files != null) {
                for (File file : files) {
                    cpEntries.add(file.getAbsolutePath());
                }
            }
        }
        return cpEntries.toArray(String[]::new);
    }

}
