package me.lenglet.sensitive;

public static class SensitiveString {

  private final String value;

  public SensitiveString(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Sensitive {
}

public static class Dto {

  @Sensitive
  private String value1;
  private String value2;
  @Sensitive
  private String value3;

  public String value1() {
    return this.value1;
  }

  public String value2() {
    return this.value2;
  }

  public String getValue3() {
    return value3;
  }
}

public static record Record(
  @Sensitive String value1,
  String value2
) {
}

public static class Util {
  public static SensitiveString fromString(String str) {
    return str == null ? null : new SensitiveString(str);
  }

  public static String fromSensitiveString(SensitiveString str) {
    return str == null ? null : str.getValue();
  }
}

class MyClass {

  public void doSomething() {
    new SensitiveString(Util.fromSensitiveString(null));
    final var ss = Util.fromSensitiveString(null);

    final var v = new Dto();

    Util.fromString(v.value2()); // Noncompliant
    Util.fromString(v.getValue3());
    Util.fromString(v.value1());
    Util.fromString("jack"); // Noncompliant

    final var r = new Record("jack", "jack");
    new SensitiveString(r.value1());
    new SensitiveString(r.value2()); // Noncompliant

    final var str = "jack";
    new SensitiveString(str); // Noncompliant


    new SensitiveString("jack"); // Noncompliant
    new SensitiveString(v.value1());
    new SensitiveString(v.value2()); // Noncompliant
    new SensitiveString(v.getValue3());

    final var sense = v.value1();
    new SensitiveString(sense);
  }
}
