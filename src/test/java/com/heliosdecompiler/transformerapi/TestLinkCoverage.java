/*
 * © 2026 Nicolas Baumann (@nbauma109)
 *
 * Licensed under the Apache License, Version 2.0.
 */

package com.heliosdecompiler.transformerapi;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Supplier;

/**
 * Coverage fixture for source-link reconstruction edge cases that still occur in normal Java code.
 */
@CoverageMarker
public abstract class TestLinkCoverage extends TestLinkCoverageBase {

    protected transient volatile int flags;

    protected TestLinkCoverage() {
        this("coverage");
    }

    protected TestLinkCoverage(String label) {
        super(label);
    }

    public native void nativeMethod() throws IOException;

    public synchronized void ownerCalls() {
        this.localCall();
        super.baseCall();
        Helper.staticCall();
    }

    public String readShared() {
        return super.shared;
    }

    public <T> T echo(T value) {
        return value;
    }

    public void varArgMethod(@CoverageMarker String... values) {
        this.flags += values.length;
    }

    public void charLiteral() {
        char quote = '\'';
        if (quote == '\'') {
            this.flags++;
        }
    }

    public Inner createInner() {
        return new Inner();
    }

    private void localCall() {
        useSupplier(() -> new Helper(super.getLabel()).value());
        useRunnable(new Runnable() {
            @Override
            public void run() {
                Helper.staticCall();
            }
        });
    }

    private void useRunnable(Runnable runnable) {
        runnable.run();
    }

    protected void useSupplier(Supplier<String> supplier) {
        supplier.get();
    }

    protected void useCollection(java.util.List<String> values) {
        this.flags += values.size();
    }

    protected void useCollection(java.util.Set<String> values) {
        this.flags += values.size();
    }

    protected void annotatedValue(@CoverageMarker("string") java.lang.String value) {
        this.flags += value.length();
    }

    protected void annotatedValue(@CoverageMarker("number") java.lang.Integer value) {
        this.flags += value.intValue();
    }

    public class Inner extends InnerBaseCoverage {

        private final String innerLabel;

        public Inner() {
            this(TestLinkCoverage.this.getLabel());
        }

        public Inner(String innerLabel) {
            super(innerLabel);
            this.innerLabel = innerLabel;
        }

        public void run() {
            this.innerCall();
            super.baseInner();
            TestLinkCoverage.this.ownerCalls();
            TestLinkCoverage.super.baseCall();
            if (this.innerLabel != null) {
                TestLinkCoverage.this.varArgMethod(this.innerLabel);
            }
        }

        public void accept(final Inner other) {
            other.innerCall();
        }

        public void acceptRoot(final TestLinkCoverage other) {
            other.ownerCalls();
        }

        public void acceptBase(final InnerBaseCoverage base) {
            base.baseInner();
        }

        private void innerCall() {
            throw new UnsupportedOperationException();
        }
    }

    public static class Helper {

        private final String value;

        public Helper(String value) {
            this.value = value;
        }

        public static void staticCall() {
            throw new UnsupportedOperationException();
       }

        public String value() {
            return this.value;
        }
    }
}

@Target({ElementType.TYPE, ElementType.PARAMETER})
@Retention(RetentionPolicy.CLASS)
@interface CoverageMarker {
    String value() default "";
}

abstract class TestLinkCoverageBase {

    protected final String shared;

    private final String label;

    protected TestLinkCoverageBase(String label) {
        this.shared = label;
        this.label = label;
    }

    public String getLabel() {
        return this.label;
    }

    public void baseCall() {
        throw new UnsupportedOperationException();
    }
}

class InnerBaseCoverage {

    private final String label;

    InnerBaseCoverage(String label) {
        this.label = label;
    }

    public void baseInner() {
        throw new UnsupportedOperationException();
    }

    public String getLabel() {
        return this.label;
    }
}
