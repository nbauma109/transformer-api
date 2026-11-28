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

import com.heliosdecompiler.transformerapi.decompilers.fernflower.FernflowerLogger;

import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger.Severity;
import org.junit.Test;
import org.slf4j.Logger;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class DecompilerLogSupportTest {

    @Test
    public void testSeverityMappingAndFernflowerLogging() {
        String message = "Decompiler diagnostic";
        Throwable failure = new IllegalStateException(message);
        List<String> methods = new ArrayList<>();
        Logger logger = (Logger) Proxy.newProxyInstance(Logger.class.getClassLoader(),
                new Class<?>[]{Logger.class}, (_, method, arguments) -> {
                    methods.add(method.getName());
                    assertEquals(message, arguments[0]);
                    assertSame(failure, arguments[1]);
                    return null;
                });

        for (String severity : List.of("TRACE", "INFO", "WARN", "ERROR", "UNKNOWN")) {
            DecompilerLogSupport.log(logger, message, severity, failure);
        }

        assertEquals(List.of("trace", "info", "warn", "error", "info"), methods);
        verifyFernflowerLoggerAcceptsAllSeveritiesWithAndWithoutFailure();
    }

    private static void verifyFernflowerLoggerAcceptsAllSeveritiesWithAndWithoutFailure() {
        FernflowerLogger logger = new FernflowerLogger();
        Throwable failure = new IllegalStateException("Decompiler diagnostic");

        for (Severity severity : Severity.values()) {
            logger.writeMessage("Decompiler diagnostic", severity);
            logger.writeMessage("Decompiler diagnostic", severity, failure);
        }
    }
}
