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

/**
 * How decompiled source ended up (or not) realigned with the original line numbers.
 */
public enum RealignStatus {
    /** Realignment was not requested. */
    TURNED_OFF,
    /** The decompiler realigned the output itself. */
    NATIVELY_REALIGNED,
    /** The output was parsed and realigned afterwards. */
    PARSED_AND_REALIGNED,
    /** The output could not be parsed, so it was left as is. */
    PARSE_ERROR
}
