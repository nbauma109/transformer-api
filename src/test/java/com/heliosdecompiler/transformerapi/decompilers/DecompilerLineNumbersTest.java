/*
 * © 2026 Nicolas Baumann (@nbauma109)
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

import org.apache.commons.io.IOUtils;
import org.jd.core.v1.util.ZipLoader;
import org.junit.Test;

import com.heliosdecompiler.transformerapi.StandardTransformers;
import com.heliosdecompiler.transformerapi.StandardTransformers.Decompilers;
import com.heliosdecompiler.transformerapi.common.Loader;
import com.heliosdecompiler.transformerapi.decompilers.cfr.CFRSettings;
import com.heliosdecompiler.transformerapi.decompilers.fernflower.FernflowerSettings;
import com.heliosdecompiler.transformerapi.decompilers.jadx.MapJadxArgs;
import com.heliosdecompiler.transformerapi.decompilers.jd.JDSettings;
import com.heliosdecompiler.transformerapi.decompilers.procyon.MapDecompilerSettings;
import com.heliosdecompiler.transformerapi.decompilers.vineflower.VineflowerSettings;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.TreeMap;

import jd.core.DecompilationResult;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class DecompilerLineNumbersTest {

    private static final String ROOT_LOCATION = "target/test-classes";
    private static final String PACKAGE_NAME = "com/heliosdecompiler/transformerapi";
    private static final String CLASS_NAME = "TestCompact.class";
    private static final String INTERNAL_NAME = "test/TestCompact";

    @Test
    public void testAllDecompilersPopulateLineNumbersWithLineNumberSettings() throws Exception {
        assertProvidesLineNumbers(Decompilers.CFR.decompile(ROOT_LOCATION, PACKAGE_NAME, CLASS_NAME));
        assertProvidesLineNumbers(Decompilers.FERNFLOWER.decompile(ROOT_LOCATION, PACKAGE_NAME, CLASS_NAME));
        assertProvidesLineNumbers(Decompilers.VINEFLOWER.decompile(ROOT_LOCATION, PACKAGE_NAME, CLASS_NAME));
        assertProvidesLineNumbers(Decompilers.JD_CORE_V0.decompile(ROOT_LOCATION, PACKAGE_NAME, CLASS_NAME));
        assertProvidesLineNumbers(Decompilers.JD_CORE_V1.decompile(ROOT_LOCATION, PACKAGE_NAME, CLASS_NAME));
        assertProvidesLineNumbers(Decompilers.PROCYON.decompile(ROOT_LOCATION, PACKAGE_NAME, CLASS_NAME));
        assertProvidesLineNumbers(Decompilers.JADX.decompile(ROOT_LOCATION, PACKAGE_NAME, CLASS_NAME));
    }

    @Test
    public void testDecompilersPreserveLineNumbersWithoutExplicitLineNumberSettings() throws Exception {
        Loader loader = loader("/test-compact-expand-inline.jar");

        assertProvidesLineNumbers(StandardTransformers.decompile(loader, INTERNAL_NAME, CFRSettings.defaults(), Decompilers.ENGINE_CFR));
        assertProvidesLineNumbers(StandardTransformers.decompile(loader, INTERNAL_NAME, FernflowerSettings.defaults(), Decompilers.ENGINE_FERNFLOWER));
        assertProvidesLineNumbers(StandardTransformers.decompile(loader, INTERNAL_NAME, VineflowerSettings.defaults(), Decompilers.ENGINE_VINEFLOWER));
        assertProvidesLineNumbers(StandardTransformers.decompile(loader, INTERNAL_NAME, JDSettings.defaults(), Decompilers.ENGINE_JD_CORE_V0));
        assertProvidesLineNumbers(StandardTransformers.decompile(loader, INTERNAL_NAME, JDSettings.defaults(), Decompilers.ENGINE_JD_CORE_V1));
        assertProvidesLineNumbers(StandardTransformers.decompile(loader, INTERNAL_NAME, MapDecompilerSettings.defaults(), Decompilers.ENGINE_PROCYON));
        assertProvidesLineNumbers(StandardTransformers.decompile(loader, INTERNAL_NAME, MapJadxArgs.defaults(), Decompilers.ENGINE_JADX));
    }

    @Test
    public void testPutLineNumbersIgnoresNullSourceLines() {
        DecompilationResult result = new DecompilationResult();
        TreeMap<Integer, Integer> lineNumbers = new TreeMap<>();
        lineNumbers.put(1, 10);
        lineNumbers.put(2, null);
        lineNumbers.put(3, 30);

        TestDecompiler.putLineNumbers(result, lineNumbers);

        assertEquals(Integer.valueOf(10), result.getLineNumbers().get(1));
        assertFalse(result.getLineNumbers().containsKey(2));
        assertEquals(Integer.valueOf(30), result.getLineNumbers().get(3));
        assertEquals(30, result.getMaxLineNumber());
    }

    private static void assertProvidesLineNumbers(DecompilationResult result) {
        assertNotNull(result.getDecompiledOutput());
        assertFalse(result.getLineNumbers().isEmpty());
        assertTrue(result.getMaxLineNumber() > 0);
        assertTrue(result.getLineNumbers().keySet().stream().anyMatch(line -> line != null && line > 0));
        assertTrue(result.getLineNumbers().values().stream().anyMatch(line -> line != null && line > 0));
    }

    private static Loader loader(String resourcePath) throws Exception {
        try (InputStream in = DecompilerLineNumbersTest.class.getResourceAsStream(resourcePath)) {
            byte[] data = IOUtils.toByteArray(in);
            ZipLoader zipLoader = new ZipLoader(new ByteArrayInputStream(data));
            return new Loader(zipLoader::canLoad, zipLoader::load, DecompilerLineNumbersTest.class.getResource(resourcePath).toURI());
        }
    }

    private static final class TestDecompiler extends Decompiler.AbstractDecompiler {
        private TestDecompiler() {
            super("test");
        }
    }
}
