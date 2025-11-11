# Transformer API
[![Maven Central](https://img.shields.io/maven-central/v/io.github.nbauma109/transformer-api.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/io.github.nbauma109/transformer-api)
[![CodeQL](https://github.com/nbauma109/transformer-api/actions/workflows/codeql-analysis.yml/badge.svg)](https://github.com/nbauma109/transformer-api/actions/workflows/codeql-analysis.yml)
[![Maven Release](https://github.com/nbauma109/transformer-api/actions/workflows/maven.yml/badge.svg)](https://github.com/nbauma109/transformer-api/actions/workflows/maven.yml)
[![Github Release](https://github.com/nbauma109/transformer-api/actions/workflows/release.yml/badge.svg)](https://github.com/nbauma109/transformer-api/actions/workflows/release.yml)

The Transformer API provides convenient access to different transformers (currently decompilers only) under a unified
API. The API is still subject to major changes, but only with a major version bump.

## Usage

Currently, this API supports the following decompilers :

- Fernflower
- Vineflower (fork of Fernflower)
- Procyon
- CFR
- JD-Core V0 and V1
- JADX

Decompilers can be accessed either via `StandardTransformers.DECOMPILER` or by creating a new instance. They are also
stateless, which means you can use the same instance across different threads.

An example program decompiling a file using Vineflower (fork of Fernflower) is shown below:

```java
package com.heliosdecompiler.transformerapi;

import com.heliosdecompiler.transformerapi.common.Loader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class Sample {

    public byte[] load(String internalName) throws IOException {
        InputStream is = this.getClass().getResourceAsStream("/" + internalName + ".class");

        if (is == null) {
            return null;
        }
        try (InputStream in=is; ByteArrayOutputStream out=new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int read = in.read(buffer);

            while (read > 0) {
                out.write(buffer, 0, read);
                read = in.read(buffer);
            }

            return out.toByteArray();
        }
    }

    public boolean canLoad(String internalName) {
        return this.getClass().getResource("/" + internalName + ".class") != null;
    }

    
    public static void main(String[] args) {
        Sample sample = new Sample();
        Loader loader = new Loader(sample::canLoad, sample::load);
        Map<String, String> preferences = new HashMap<>();
        try {
            String ff = StandardTransformers.Decompilers.ENGINE_VINEFLOWER;
            DecompilationResult result = StandardTransformers.decompile(loader, "java/lang/String", preferences, ff);
            System.out.println(result.getDecompiledOutput());
        } catch (Exception e) {
            System.err.println(e);
        }
    }
}
```


## Features

### Updates

This API will be updated as decompilers receive updates, which means fixes reach you faster.
