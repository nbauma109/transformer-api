package com.heliosdecompiler.transformerapi.decompilers;

import org.junit.Test;

import com.heliosdecompiler.transformerapi.StandardTransformers.Decompilers;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class DecompilerTest {

    @Test
    public void testSupportsRealignment() {
        assertTrue(Decompilers.JD_CORE_V0.supportsRealignment());
        assertTrue(Decompilers.JD_CORE_V1.supportsRealignment());
        assertFalse(Decompilers.PROCYON.supportsRealignment());
        assertFalse(Decompilers.FERNFLOWER.supportsRealignment());
        assertFalse(Decompilers.VINEFLOWER.supportsRealignment());
        assertFalse(Decompilers.CFR.supportsRealignment());
        assertFalse(Decompilers.JADX.supportsRealignment());
    }

    @Test
    public void testGetDecompilerVersion() {
        assertNotNull(Decompilers.PROCYON.getDecompilerVersion());
        assertNotNull(Decompilers.CFR.getDecompilerVersion());
        assertNotNull(Decompilers.JADX.getDecompilerVersion());
    }
}
