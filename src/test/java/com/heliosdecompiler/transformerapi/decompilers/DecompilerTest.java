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
