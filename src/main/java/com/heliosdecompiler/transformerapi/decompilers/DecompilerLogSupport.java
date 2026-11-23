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

import org.slf4j.Logger;

/**
 * Maps decompiler logging severities to the application logger.
 */
public final class DecompilerLogSupport {

    private DecompilerLogSupport() {
    }

    public static void log(Logger logger, String message, String severity, Throwable failure) {
        switch (severity) {
            case "TRACE" -> logger.trace(message, failure);
            case "INFO" -> logger.info(message, failure);
            case "WARN" -> logger.warn(message, failure);
            case "ERROR" -> logger.error(message, failure);
            default -> logger.info(message, failure);
        }
    }
}
