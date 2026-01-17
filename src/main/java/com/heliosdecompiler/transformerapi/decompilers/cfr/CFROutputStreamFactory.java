/*******************************************************************************
 * Copyright (C) 2022 GPLv3
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package com.heliosdecompiler.transformerapi.decompilers.cfr;

import org.benf.cfr.reader.api.OutputSinkFactory;
import org.benf.cfr.reader.api.SinkReturns.LineNumberMapping;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.Map.Entry;

public class CFROutputStreamFactory implements OutputSinkFactory {

    private StringBuilder sb = new StringBuilder();
    private NavigableMap<Integer, Integer> lineMapping = new TreeMap<>();

    @Override
    public List<SinkClass> getSupportedSinks(SinkType sinkType, Collection<SinkClass> collection) {
        return Arrays.asList(SinkClass.STRING, SinkClass.DECOMPILED, SinkClass.DECOMPILED_MULTIVER,
                SinkClass.EXCEPTION_MESSAGE, SinkClass.LINE_NUMBER_MAPPING);
    }

    @Override
    public <T> Sink<T> getSink(SinkType sinkType, SinkClass sinkClass) {
        return sinkable -> {
            if (sinkType == SinkType.PROGRESS) {
                return;
            }
            if (sinkType == SinkType.LINENUMBER) {
                LineNumberMapping mapping = (LineNumberMapping) sinkable;
                Map<Integer, Integer> classFileMappings = mapping.getClassFileMappings();
                Map<Integer, Integer> mappings = mapping.getMappings();
                if (classFileMappings != null && mappings != null) {
                    for (Entry<Integer, Integer> entry : mappings.entrySet()) {
                        Integer srcLineNumber = classFileMappings.get(entry.getKey());
                        lineMapping.put(entry.getValue(), srcLineNumber);
                    }
                }
                return;
            }
            sb.append(sinkable);
        };
    }

    public String getGeneratedSource() {
        return sb.toString();
    }

    public Map<Integer, Integer> getLineMapping() {
        return lineMapping;
    }
}