package com.heliosdecompiler.transformerapi;

public abstract class TestCompact
{
  public void method1(boolean flag) {
    log("Test.method1 start");
    if (flag) {
      log("Test.method1 if");
    } else {
        log("Test.method1 else");
    }
    log("Test.method1 end");
  }

  public void method2() {
    log("Test.method2 start");
    try {
      log("Test.method2 try");
    } catch (Exception e) {
      log("Test.method2 catch");
    } finally {
      log("Test.method2 finally");
    }
    log("Test.method2 end");
  }

  class Inner1
  {
    public Inner1() {
      TestCompact.this.log("Inner1 constructor");
    }

    public void method1(boolean flag) {
      TestCompact.this.log("Inner1.method1 start");
      while (flag) {
        TestCompact.this.log("Inner1.method1 while");
      }
      TestCompact.this.log("Inner1.method1 end");
    }

    public void method2(boolean flag) {
      TestCompact.this.log("Inner1.method2 start"); while (true) {
        TestCompact.this.log("Inner1.method2 do while");
        if (!flag) {
          TestCompact.this.log("Inner1.method2 end");
          return;
        }
      }
    } } public void method3(int[] arr) {
    log("Test.method3 start");
    for (int i = 0; i < arr.length; i++) {
        log("Test.method3 for");
    }
    log("Test.method3 end");
  }

  public void method4(int i) {
    log("Test.method4 start");
    switch (i) {
      case 0:
        log("Test.method4 case 0");
        break;
      case 1:
        log("Test.method4 case 1");
        break;
      case 2:
        log("Test.method4 case 2");
        break;
      case 3:
        log("Test.method4 case 3");
        break;
      default:
        log("Test.method4 default");
        break;
    }
    log("Test.method4 end");
  }

  public String method5(int i) {
    log("Test.method5 start");
    String s = switch (i) {
        case 0 -> "Test.method4 case 0";
        case 1 -> "Test.method4 case 1";
        case 2 -> "Test.method4 case 2";
        case 3 -> "Test.method4 case 3";
        default -> throw new IllegalArgumentException();
      };
    return "return:" + s;
  }

  class Inner3
  {
    public Inner3() {
      TestCompact.this.log("Inner3 constructor");
    }

    public void method1() {
      new Thread(() -> TestCompact.this.log("Inner3.method1")).start();
    }

    public void method2() {
      new Thread(() -> {
            TestCompact.this.log("Inner3.method2a");
            TestCompact.this.log("Inner3.method2b");
          }).start();
    }

    public void method3() {
      new Thread(() -> TestCompact.this.log("Inner3.method3")).start();
    }
  }

  class Inner4
  {
    public Inner4() {
      TestCompact.this.log("Inner4 constructor");
    }

    public void method1() {
      TestCompact.this.log("Inner4.method1");
    }

    public void method2() {
      TestCompact.this.log("Inner4.method2");
    }


    public boolean equals(Object o) {
      throw new UnsupportedOperationException("equals");
    }


    public int hashCode() {
      throw new UnsupportedOperationException("hashCode");
    }


    public String toString() {
      throw new UnsupportedOperationException("toString");
    }
  }

  public void method5() {
    log("Test.method5");
  }

  public void method6() {
    log("Test.method6");
  }

  public abstract void log(String paramString);
}
