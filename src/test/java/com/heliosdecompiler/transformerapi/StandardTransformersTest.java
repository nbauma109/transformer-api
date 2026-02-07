package com.heliosdecompiler.transformerapi;

import org.apache.commons.io.IOUtils;
import org.jd.core.v1.loader.ClassPathLoader;
import org.jd.core.v1.util.ZipLoader;
import org.junit.Test;

import com.heliosdecompiler.transformerapi.common.Loader;
import com.heliosdecompiler.transformerapi.decompilers.Decompiler;
import com.heliosdecompiler.transformerapi.decompilers.cfr.CFRSettings;
import com.heliosdecompiler.transformerapi.decompilers.fernflower.FernflowerSettings;
import com.heliosdecompiler.transformerapi.decompilers.jadx.MapJadxArgs;
import com.heliosdecompiler.transformerapi.decompilers.jd.JDSettings;
import com.heliosdecompiler.transformerapi.decompilers.procyon.MapDecompilerSettings;
import com.heliosdecompiler.transformerapi.decompilers.vineflower.VineflowerSettings;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

import static com.heliosdecompiler.transformerapi.StandardTransformers.Decompilers.CFR;
import static com.heliosdecompiler.transformerapi.StandardTransformers.Decompilers.ENGINE_CFR;
import static com.heliosdecompiler.transformerapi.StandardTransformers.Decompilers.ENGINE_FERNFLOWER;
import static com.heliosdecompiler.transformerapi.StandardTransformers.Decompilers.ENGINE_JADX;
import static com.heliosdecompiler.transformerapi.StandardTransformers.Decompilers.ENGINE_JD_CORE;
import static com.heliosdecompiler.transformerapi.StandardTransformers.Decompilers.ENGINE_JD_CORE_V0;
import static com.heliosdecompiler.transformerapi.StandardTransformers.Decompilers.ENGINE_JD_CORE_V1;
import static com.heliosdecompiler.transformerapi.StandardTransformers.Decompilers.ENGINE_PROCYON;
import static com.heliosdecompiler.transformerapi.StandardTransformers.Decompilers.ENGINE_VINEFLOWER;
import static com.heliosdecompiler.transformerapi.StandardTransformers.Decompilers.FERNFLOWER;
import static com.heliosdecompiler.transformerapi.StandardTransformers.Decompilers.JADX;
import static com.heliosdecompiler.transformerapi.StandardTransformers.Decompilers.JD_CORE_V0;
import static com.heliosdecompiler.transformerapi.StandardTransformers.Decompilers.JD_CORE_V1;
import static com.heliosdecompiler.transformerapi.StandardTransformers.Decompilers.PROCYON;
import static com.heliosdecompiler.transformerapi.StandardTransformers.Decompilers.VINEFLOWER;
import static com.heliosdecompiler.transformerapi.StandardTransformers.Decompilers.valueOf;
import static org.junit.Assert.assertEquals;

import jd.core.DecompilationResult;

public class StandardTransformersTest {

    @Test
    public void testValueOf() {
        assertEquals(VINEFLOWER, valueOf(""));
        assertEquals(VINEFLOWER, valueOf(ENGINE_VINEFLOWER));
        assertEquals(FERNFLOWER, valueOf(ENGINE_FERNFLOWER));
        assertEquals(JD_CORE_V0, valueOf(ENGINE_JD_CORE_V0));
        assertEquals(JD_CORE_V1, valueOf(ENGINE_JD_CORE_V1));
        assertEquals(JD_CORE_V1, valueOf(ENGINE_JD_CORE));
        assertEquals(CFR, valueOf(ENGINE_CFR));
        assertEquals(PROCYON, valueOf(ENGINE_PROCYON));
        assertEquals(JADX, valueOf(ENGINE_JADX));
    }

    @Test
    public void testDecompileCFR() throws Exception {
        testDecompile("/TestCompactCFR.txt", ENGINE_CFR, CFRSettings.defaults(), "test/TestCompact", "/test-compact-expand-inline.jar");
    }

    @Test
    public void testDecompileCFRBootInf() throws Exception {
        testDecompile("/TestCompactCFRFromRootLocation.txt", ENGINE_CFR, CFRSettings.lineNumbers(), "BOOT-INF/classes/com/heliosdecompiler/transformerapi/TestCompact", "/test-boot-inf-classes.jar");
    }
    
    @Test
    public void testDecompileCFRWebInf() throws Exception {
        testDecompile("/TestCompactCFRFromRootLocation.txt", ENGINE_CFR, CFRSettings.lineNumbers(), "WEB-INF/classes/com/heliosdecompiler/transformerapi/TestCompact", "/test-web-inf-classes.jar");
    }
    
    @Test
    public void testDecompileCFRWithLineNumbers() throws Exception {
        testDecompile("/TestCompactCFRWithLineNumbers.txt", ENGINE_CFR, CFRSettings.lineNumbers(), "test/TestCompact", "/test-compact-expand-inline.jar");
    }

    @Test
    public void testDecompileCFRFromArchive() throws Exception {
        testDecompileFromArchive("/TestCompactCFRWithLineNumbers.txt", CFR);
    }
    
    @Test
    public void testDecompileCFRFromRootLocation() throws Exception {
        testDecompileFromRootLocation("/TestCompactCFRFromRootLocation.txt", CFR);
    }
    
    @Test
    public void testDecompileCFRFromClassPath() throws Exception {
        testDecompileFromClassPath("/TestThrowableCFR.txt", ENGINE_CFR, CFRSettings.defaults());
    }
    
    @Test
    public void testDecompileProcyonFromClassPath() throws Exception {
        testDecompileFromClassPath("/TestThrowableProcyon.txt", ENGINE_PROCYON, MapDecompilerSettings.defaults());
    }

    @Test
    public void testDecompileProcyon() throws Exception {
        testDecompile("/TestCompactProcyon.txt", ENGINE_PROCYON, MapDecompilerSettings.defaults(), "test/TestCompact", "/test-compact-expand-inline.jar");
    }

    @Test
    public void testDecompileProcyonBootInf() throws Exception {
        testDecompile("/TestCompactProcyonFromRootLocation.txt", ENGINE_PROCYON, MapDecompilerSettings.lineNumbers(), "BOOT-INF/classes/com/heliosdecompiler/transformerapi/TestCompact", "/test-boot-inf-classes.jar");
    }
    
    @Test
    public void testDecompileProcyonWebInf() throws Exception {
        testDecompile("/TestCompactProcyonFromRootLocation.txt", ENGINE_PROCYON, MapDecompilerSettings.lineNumbers(), "WEB-INF/classes/com/heliosdecompiler/transformerapi/TestCompact", "/test-web-inf-classes.jar");
    }
    
    @Test
    public void testDecompileProcyonWithLineNumbers() throws Exception {
        testDecompile("/TestCompactProcyonWithLineNumbers.txt", ENGINE_PROCYON, MapDecompilerSettings.lineNumbers(), "test/TestCompact", "/test-compact-expand-inline.jar");
    }

    @Test
    public void testDecompileProcyonFromArchive() throws Exception {
        testDecompileFromArchive("/TestCompactProcyonWithLineNumbers.txt", PROCYON);
    }
    
    @Test
    public void testDecompileProcyonFromRootLocation() throws Exception {
        testDecompileFromRootLocation("/TestCompactProcyonFromRootLocation.txt", PROCYON);
    }

    @Test
    public void testDecompileProcyonByteCode() throws Exception {
        testDecompile("/TestCompactProcyonByteCode.txt", ENGINE_PROCYON, MapDecompilerSettings.byteCodeSettings(), "test/TestCompact", "/test-compact-expand-inline.jar");
    }

    @Test
    public void testDecompileJADXFromClassPath() throws Exception {
        testDecompileFromClassPath("/TestThrowableJADX.txt", ENGINE_JADX, Collections.emptyMap());
    }

    @Test
    public void testDecompileJADX() throws Exception {
        testDecompile("/TestCompactJADX.txt", ENGINE_JADX, MapJadxArgs.defaults(), "test/TestCompact", "/test-compact-expand-inline.jar");
    }

    @Test
    public void testDecompileJADXBootInf() throws Exception {
        testDecompile("/TestCompactJADXFromRootLocation.txt", ENGINE_JADX, MapJadxArgs.lineNumbers(), "BOOT-INF/classes/com/heliosdecompiler/transformerapi/TestCompact", "/test-boot-inf-classes.jar");
    }
    
    @Test
    public void testDecompileJADXWebInf() throws Exception {
        testDecompile("/TestCompactJADXFromRootLocation.txt", ENGINE_JADX, MapJadxArgs.lineNumbers(), "WEB-INF/classes/com/heliosdecompiler/transformerapi/TestCompact", "/test-web-inf-classes.jar");
    }
    
    @Test
    public void testDecompileJADXWithLineNumbers() throws Exception {
        testDecompile("/TestCompactJADXWithLineNumbers.txt", ENGINE_JADX, MapJadxArgs.lineNumbers(), "test/TestCompact", "/test-compact-expand-inline.jar");
    }

    @Test
    public void testDecompileJADXFromArchive() throws Exception {
        testDecompileFromArchive("/TestCompactJADXWithLineNumbers.txt", JADX);
    }
    
    @Test
    public void testDecompileJADXFromRootLocation() throws Exception {
        testDecompileFromRootLocation("/TestCompactJADXFromRootLocation.txt", JADX);
    }

    @Test
    public void testDecompileFernflower() throws Exception {
        testDecompile("/TestCompactFernflower.txt", ENGINE_FERNFLOWER, Collections.emptyMap(), "test/TestCompact", "/test-compact-expand-inline.jar");
    }

    @Test
    public void testDecompileFernflowerBootInf() throws Exception {
        testDecompile("/TestCompactFernflowerFromRootLocation.txt", ENGINE_FERNFLOWER, FernflowerSettings.lineNumbers(), "BOOT-INF/classes/com/heliosdecompiler/transformerapi/TestCompact", "/test-boot-inf-classes.jar");
    }
    
    @Test
    public void testDecompileFernflowerWebInf() throws Exception {
        testDecompile("/TestCompactFernflowerFromRootLocation.txt", ENGINE_FERNFLOWER, FernflowerSettings.lineNumbers(), "WEB-INF/classes/com/heliosdecompiler/transformerapi/TestCompact", "/test-web-inf-classes.jar");
    }
    
    @Test
    public void testDecompileFernflowerFromClassPath() throws Exception {
        testDecompileFromClassPath("/TestThrowableFernflower.txt", ENGINE_FERNFLOWER, Collections.emptyMap());
    }

    @Test
    public void testDecompileFernflowerWithLineNumbers() throws Exception {
        testDecompile("/TestCompactFernflowerWithLineNumbers.txt", ENGINE_FERNFLOWER, FernflowerSettings.lineNumbers(), "test/TestCompact", "/test-compact-expand-inline.jar");
    }

    @Test
    public void testDecompileFernflowerFromArchive() throws Exception {
        testDecompileFromArchive("/TestCompactFernflowerWithLineNumbers.txt", FERNFLOWER);
    }
    
    @Test
    public void testDecompileFernflowerFromRootLocation() throws Exception {
        testDecompileFromRootLocation("/TestCompactFernflowerFromRootLocation.txt", FERNFLOWER);
    }

    @Test
    public void testDecompileVineflower() throws Exception {
        testDecompile("/TestCompactVineflower.txt", ENGINE_VINEFLOWER, Collections.emptyMap(), "test/TestCompact", "/test-compact-expand-inline.jar");
    }

    @Test
    public void testDecompileVineflowerBootInf() throws Exception {
        testDecompile("/TestCompactVineflowerFromRootLocation.txt", ENGINE_VINEFLOWER, VineflowerSettings.lineNumbers(), "BOOT-INF/classes/com/heliosdecompiler/transformerapi/TestCompact", "/test-boot-inf-classes.jar");
    }
    
    @Test
    public void testDecompileVineflowerWebInf() throws Exception {
        testDecompile("/TestCompactVineflowerFromRootLocation.txt", ENGINE_VINEFLOWER, VineflowerSettings.lineNumbers(), "WEB-INF/classes/com/heliosdecompiler/transformerapi/TestCompact", "/test-web-inf-classes.jar");
    }
    
    @Test
    public void testDecompileVineflowerFromClassPath() throws Exception {
        testDecompileFromClassPath("/TestThrowableVineflower.txt", ENGINE_VINEFLOWER, Collections.emptyMap());
    }

    @Test
    public void testDecompileVineflowerWithLineNumbers() throws Exception {
        testDecompile("/TestCompactVineflowerWithLineNumbers.txt", ENGINE_VINEFLOWER, VineflowerSettings.lineNumbers(), "test/TestCompact", "/test-compact-expand-inline.jar");
    }

    @Test
    public void testDecompileVineflowerFromArchive() throws Exception {
        testDecompileFromArchive("/TestCompactVineflowerWithLineNumbers.txt", VINEFLOWER);
    }
    
    @Test
    public void testDecompileVineflowerFromRootLocation() throws Exception {
        testDecompileFromRootLocation("/TestCompactVineflowerFromRootLocation.txt", VINEFLOWER);
    }

    @Test
    public void testDecompileJDCoreV0() throws Exception {
        testDecompile("/TestCompactJDCoreV0.txt", ENGINE_JD_CORE_V0, JDSettings.defaults(), "test/TestCompact", "/test-compact-expand-inline.jar");
    }

    @Test
    public void testDecompileJDCoreV0BootInf() throws Exception {
        testDecompile("/TestCompactJDCoreV0FromRootLocation.txt", ENGINE_JD_CORE_V0, JDSettings.lineNumbers(), "BOOT-INF/classes/com/heliosdecompiler/transformerapi/TestCompact", "/test-boot-inf-classes.jar");
    }
    
    @Test
    public void testDecompileJDCoreV0WebInf() throws Exception {
        testDecompile("/TestCompactJDCoreV0FromRootLocation.txt", ENGINE_JD_CORE_V0, JDSettings.lineNumbers(), "WEB-INF/classes/com/heliosdecompiler/transformerapi/TestCompact", "/test-web-inf-classes.jar");
    }
    
    @Test
    public void testDecompileJDCoreV0FromClassPath() throws Exception {
        testDecompileFromClassPath("/TestThrowableJDCoreV0.txt", ENGINE_JD_CORE_V0, JDSettings.defaults());
    }

    @Test
    public void testDecompileJDCoreV0WithLineNumbers() throws Exception {
        testDecompile("/TestCompactJDCoreV0WithLineNumbers.txt", ENGINE_JD_CORE_V0, JDSettings.lineNumbers(), "test/TestCompact", "/test-compact-expand-inline.jar");
    }

    @Test
    public void testDecompileJDCoreV0FromArchive() throws Exception {
        testDecompileFromArchive("/TestCompactJDCoreV0WithLineNumbers.txt", JD_CORE_V0);
    }
    
    @Test
    public void testDecompileJDCoreV0FromRootLocation() throws Exception {
        testDecompileFromRootLocation("/TestCompactJDCoreV0FromRootLocation.txt", JD_CORE_V0);
    }

    @Test
    public void testDecompileJDCoreV1() throws Exception {
        testDecompile("/TestCompactJDCoreV1.txt", ENGINE_JD_CORE_V1, JDSettings.defaults(), "test/TestCompact", "/test-compact-expand-inline.jar");
    }

    @Test
    public void testDecompileJDCoreV1BootInf() throws Exception {
        testDecompile("/TestCompactJDCoreV1FromRootLocation.txt", ENGINE_JD_CORE_V1, JDSettings.lineNumbers(), "BOOT-INF/classes/com/heliosdecompiler/transformerapi/TestCompact", "/test-boot-inf-classes.jar");
    }
    
    @Test
    public void testDecompileJDCoreV1WebInf() throws Exception {
        testDecompile("/TestCompactJDCoreV1FromRootLocation.txt", ENGINE_JD_CORE_V1, JDSettings.lineNumbers(), "WEB-INF/classes/com/heliosdecompiler/transformerapi/TestCompact", "/test-web-inf-classes.jar");
    }
    
    @Test
    public void testDecompileJDCoreV1FromClassPath() throws Exception {
        testDecompileFromClassPath("/TestThrowableJDCoreV1.txt", ENGINE_JD_CORE_V1, JDSettings.defaults());
    }

    @Test
    public void testDecompileJDCoreV1WithLineNumbers() throws Exception {
        testDecompile("/TestCompactJDCoreV1WithLineNumbers.txt", ENGINE_JD_CORE_V1, JDSettings.lineNumbers(), "test/TestCompact", "/test-compact-expand-inline.jar");
    }
    
    @Test
    public void testDecompileJDCoreV1FromArchive() throws Exception {
        testDecompileFromArchive("/TestCompactJDCoreV1WithLineNumbers.txt", JD_CORE_V1);
    }
    
    @Test
    public void testDecompileJDCoreV1FromRootLocation() throws Exception {
        testDecompileFromRootLocation("/TestCompactJDCoreV1FromRootLocation.txt", JD_CORE_V1);
    }

    private void testDecompile(String path, String engineName, Map<String, String> preferences, String internalName, String jarPath)
            throws IOException, IllegalAccessException, InvocationTargetException, URISyntaxException {
        URI resource = getClass().getResource(jarPath).toURI();
        ZipLoader zipLoader = new ZipLoader(resource.toURL().openStream());
        Loader loader = new Loader(zipLoader::canLoad, zipLoader::load, resource);
        DecompilationResult result = StandardTransformers.decompile(loader, internalName, preferences, engineName);
        assertEqualsIgnoreEOL(getResourceAsString(path), result.getDecompiledOutput());
    }
    
    private void testDecompileFromArchive(String path, Decompiler<?> decompiler)
            throws IOException, IllegalAccessException, InvocationTargetException {
        String archivePath = "src/test/resources/test-compact-expand-inline.jar";
        String pkg = "test";
        String className = "TestCompact.class";
        DecompilationResult result = decompiler.decompileFromArchive(archivePath, pkg, className);
        assertEqualsIgnoreEOL(getResourceAsString(path), result.getDecompiledOutput());
    }
    
    private void testDecompileFromRootLocation(String path, Decompiler<?> decompiler)
            throws IOException, IllegalAccessException, InvocationTargetException {
        String rootLocation = "target/test-classes";
        String pkg = "com/heliosdecompiler/transformerapi";
        String className = "TestCompact.class";
        DecompilationResult result = decompiler.decompile(rootLocation, pkg, className);
        assertEqualsIgnoreEOL(getResourceAsString(path), result.getDecompiledOutput());
    }
    
    private void testDecompileFromClassPath(String path, String engineName, Map<String, String> preferences)
            throws IOException, IllegalAccessException, InvocationTargetException {
        ClassPathLoader classPathLoader = new ClassPathLoader();
        Loader loader = new Loader(classPathLoader::canLoad, classPathLoader::load);
        String internalName = "java/lang/Throwable";
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
