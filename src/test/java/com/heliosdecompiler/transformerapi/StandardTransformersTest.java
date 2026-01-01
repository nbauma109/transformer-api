package com.heliosdecompiler.transformerapi;

import org.apache.commons.io.IOUtils;
import org.jd.core.v1.loader.ClassPathLoader;
import org.junit.Test;

import com.heliosdecompiler.transformerapi.common.Loader;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

import jd.core.DecompilationResult;

import static org.junit.Assert.assertEquals;
import static com.heliosdecompiler.transformerapi.StandardTransformers.Decompilers.ENGINE_CFR;
import static com.heliosdecompiler.transformerapi.StandardTransformers.Decompilers.ENGINE_PROCYON;
import static com.heliosdecompiler.transformerapi.StandardTransformers.Decompilers.ENGINE_FERNFLOWER;
import static com.heliosdecompiler.transformerapi.StandardTransformers.Decompilers.ENGINE_VINEFLOWER;
import static com.heliosdecompiler.transformerapi.StandardTransformers.Decompilers.ENGINE_JADX;
import static com.heliosdecompiler.transformerapi.StandardTransformers.Decompilers.ENGINE_JD_CORE_V0;
import static com.heliosdecompiler.transformerapi.StandardTransformers.Decompilers.ENGINE_JD_CORE_V1;

import static jd.core.preferences.Preferences.WRITE_METADATA;
import static jd.core.preferences.Preferences.WRITE_LINE_NUMBERS;

public class StandardTransformersTest {

    @Test
    public void testDecompileCFR() throws Exception {
        testDecompile("/HelloWorldCFR.txt", ENGINE_CFR, Map.of("showversion", "false"));
    }

    @Test
    public void testDecompileProcyon() throws Exception {
        testDecompile("/HelloWorldProcyon.txt", ENGINE_PROCYON, Map.of("SuppressBanner", "true"));
    }

    @Test
    public void testDecompileJADX() throws Exception {
        testDecompile("/HelloWorldJADX.txt", ENGINE_JADX, Collections.emptyMap());
    }

    @Test
    public void testDecompileFernflower() throws Exception {
        testDecompile("/HelloWorldFernflower.txt", ENGINE_FERNFLOWER, Collections.emptyMap());
    }

    @Test
    public void testDecompileVineflower() throws Exception {
        testDecompile("/HelloWorldVineflower.txt", ENGINE_VINEFLOWER, Collections.emptyMap());
    }

    @Test
    public void testDecompileJDCoreV0() throws Exception {
        testDecompile("/HelloWorldJDCoreV0.txt", ENGINE_JD_CORE_V0, Collections.emptyMap());
    }

    @Test
    public void testDecompileJDCoreV1() throws Exception {
        testDecompile("/HelloWorldJDCoreV1.txt", ENGINE_JD_CORE_V1, Map.of(WRITE_LINE_NUMBERS, "false", WRITE_METADATA, "false"));
    }

    private void testDecompile(String path, String engineName, Map<String, String> preferences)
            throws TransformationException, IOException, IllegalAccessException, InvocationTargetException {
        ClassPathLoader classPathLoader = new ClassPathLoader();
        Loader loader = new Loader(classPathLoader::canLoad, classPathLoader::load);
        String internalName = HelloWorld.class.getName().replace('.', '/');
        DecompilationResult result = StandardTransformers.decompile(loader, internalName, preferences, engineName);
        assertEqualsIgnoreEOL(getResourceAsString(path), result.getDecompiledOutput());
    }

    private void assertEqualsIgnoreEOL(String expected, String actual) {
        assertEquals(expected.replaceAll("\s*\r?\n", "\n"), actual.replaceAll("\s*\r?\n", "\n"));
    }

    private String getResourceAsString(String path) throws IOException {
        return IOUtils.toString(getClass().getResource(path), StandardCharsets.UTF_8);
    }
}
