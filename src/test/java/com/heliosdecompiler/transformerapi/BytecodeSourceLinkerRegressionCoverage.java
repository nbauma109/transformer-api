/*
 * Copyright 2026 Nicolas Baumann (@nbauma109)
 *
 * Licensed under the Apache License, Version 2.0.
 */

package com.heliosdecompiler.transformerapi;

public class BytecodeSourceLinkerRegressionCoverage {

    public void arrayTarget(String value) {
    }

    public void genericTarget(String value) {
    }

    public void nestedArrayArgument() {
        arrayTarget(new String[]{"a", "b"}[0]);
    }

    public void nestedGenericArgument() {
        genericTarget(new java.util.HashMap<String, Integer>().toString());
    }

    public void escapedStringLiteral() {
        String value = "\\\\";
        if (value != null) {
            escapedTarget();
        }
    }

    private void escapedTarget() {
    }

    public void afterEscapedString() {
        afterEscapedTarget();
    }

    private void afterEscapedTarget() {
    }
}
