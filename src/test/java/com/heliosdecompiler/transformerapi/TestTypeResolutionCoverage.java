/*
 * Copyright 2026 Nicolas Baumann (@nbauma109)
 *
 * Licensed under the Apache License, Version 2.0.
 */

package com.heliosdecompiler.transformerapi;

public class TestTypeResolutionCoverage extends TypeResolutionBase {

    @SuppressWarnings("all")
    public void run() {
    }

    public static class SharedType {
    }

    public class Inner {

        public void acceptRoot(TestTypeResolutionCoverage other) {
            other.run();
        }

        public void acceptAmbiguous(SharedType value) {
            if (value != null) {
                value.toString();
            }
        }
    }
}

class TypeResolutionBase {

    @SuppressWarnings("all")
    private class TestTypeResolutionCoverage {
    }

    @SuppressWarnings("all")
    private class SharedType {
    }
}
