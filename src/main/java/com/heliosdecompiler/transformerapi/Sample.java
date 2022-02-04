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
        } else {
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
    }

    public boolean canLoad(String internalName) {
        return this.getClass().getResource("/" + internalName + ".class") != null;
    }

    
    public static void main(String[] args) {
        Sample sample = new Sample();
        Loader loader = new Loader(sample::canLoad, sample::load);
        Map<String, String> preferences = new HashMap<>();
        try {
            String out = StandardTransformers.decompile(loader, "java/lang/String", preferences, StandardTransformers.Decompilers.ENGINE_FERNFLOWER);
            System.out.println(out);
        } catch (TransformationException | IOException e) {
            System.err.println(e);
        }
    }
}
