package com.heliosdecompiler.transformerapi.decompilers.cfr;

import org.benf.cfr.reader.api.OutputSinkFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class CFROutputStreamFactory implements OutputSinkFactory {
    private String generatedSource;

    @Override
    public List<SinkClass> getSupportedSinks(SinkType sinkType, Collection<SinkClass> collection) {
        return Collections.singletonList(SinkClass.STRING);
    }

    @Override
    public <T> Sink<T> getSink(SinkType sinkType, SinkClass sinkClass) {
        return a -> generatedSource = (String) a;
    }

    public String getGeneratedSource() {
        return generatedSource;
    }
}