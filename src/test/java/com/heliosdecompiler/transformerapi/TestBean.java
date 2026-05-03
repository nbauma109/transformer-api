/*
 * © 2026 Nicolas Baumann (@nbauma109)
 *
 * Licensed under the Apache License, Version 2.0.
 */

package com.heliosdecompiler.transformerapi;

/**
 * Bean-shaped fixture used to verify links for fields, constructors, allocations, and inner classes.
 */
public class TestBean extends TestBeanBase {

    private final String name;

    private final int age;

    private final Helper helper;

    public TestBean() {
        this("bean", 42);
    }

    public TestBean(String name, int age) {
        this(name, age, new Helper(name));
    }

    public TestBean(String name, int age, Helper helper) {
        super(name);
        this.name = name;
        this.age = age;
        this.helper = helper;
    }

    public String getName() {
        return this.name;
    }

    public int getAge() {
        return this.age;
    }

    public Helper getHelper() {
        return this.helper;
    }

    public TestBean copy() {
        return new TestBean(this.name, this.age, new Helper(this.name));
    }

    public InnerBean newInner(String value) {
        return new InnerBean(new Helper(value));
    }

    public class InnerBean extends InnerBase {

        private final Helper innerHelper;

        public InnerBean() {
            this(new Helper(TestBean.this.getName()));
        }

        public InnerBean(Helper innerHelper) {
            super(innerHelper);
            this.innerHelper = innerHelper;
        }

        public Helper getInnerHelper() {
            return this.innerHelper;
        }
    }

    public static class Helper {

        private final String value;

        public Helper(String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }
    }
}

abstract class TestBeanBase {

    private final String label;

    protected TestBeanBase(String label) {
        this.label = label;
    }

    public String getLabel() {
        return this.label;
    }
}

class InnerBase {

    private final TestBean.Helper helper;

    InnerBase(TestBean.Helper helper) {
        this.helper = helper;
    }

    public TestBean.Helper getHelper() {
        return this.helper;
    }
}
