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

package com.heliosdecompiler.transformerapi.decompilers.vineflower;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bridges the Vineflower logging interface to the application logger.
 */
public final class VineflowerLogger extends org.vineflower.java.decompiler.main.extern.IFernflowerLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger(VineflowerLogger.class);

    @Override
    public void writeMessage(String message, Severity severity) {
        writeMessage(message, severity, null);
    }

    @Override
    public void writeMessage(String message, Severity severity, Throwable failure) {
        switch (severity.name()) {
            case "TRACE" -> LOGGER.trace(message, failure);
            case "INFO" -> LOGGER.info(message, failure);
            case "WARN" -> LOGGER.warn(message, failure);
            case "ERROR" -> LOGGER.error(message, failure);
            default -> LOGGER.info(message, failure);
        }
    }
}
