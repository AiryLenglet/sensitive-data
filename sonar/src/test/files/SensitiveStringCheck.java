package me.lenglet;

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

public static class ParentDto {

    @Sensitive
    private String parentValue1;
    private String parentValue2;

    public String getParentValue1() {
        return parentValue1;
    }

    public void setParentValue1(String parentValue1) {
        this.parentValue1 = parentValue1;
    }

    public String getParentValue2() {
        return parentValue2;
    }

    public void setParentValue2(String parentValue2) {
        this.parentValue2 = parentValue2;
    }
}

public static class WrapperDto {
    private Dto nested;

    public Dto getNested() {
        return nested;
    }

    public void setNested(me.lenglet.sensitive.Dto nested) {
        this.nested = nested;
    }
}

public static class Dto extends ParentDto {

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

    public void setValue1(String value1) {
        this.value1 = value1;
    }

    public void setValue2(String value2) {
        this.value2 = value2;
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

        final var wrapper = new WrapperDto();
        new SensitiveString(wrapper.getNested().value2()); // Noncompliant
        new SensitiveString(wrapper.getNested().value1()); // Compliant

        new SensitiveString(Util.fromSensitiveString(null));
        final var ss = Util.fromSensitiveString(null);

        final var dto = new Dto();
        final var dto2 = new Dto();

        dto2.setValue2(dto.value1()); // Noncompliant
        dto2.setValue1(dto.value2()); // Noncompliant
        dto2.setValue1(dto.value1()); // Compliant

        Util.fromString(dto.value2()); // Noncompliant
        Util.fromString(dto.getValue3());
        Util.fromString(dto.value1());
        Util.fromString("jack"); // Noncompliant

        final var r = new Record("jack", "jack");
        new SensitiveString(r.value1());
        new SensitiveString(r.value2()); // Noncompliant

        final var str = "jack";
        new SensitiveString(str); // Noncompliant
        new SensitiveString("jack"); // Noncompliant
        new SensitiveString(dto.value1());
        new SensitiveString(dto.value2()); // Noncompliant
        new SensitiveString(dto.getValue3());
        new SensitiveString(dto.getParentValue2()); // Noncompliant
        new SensitiveString(dto.getParentValue1());

        final var sense = dto.value1();
        new SensitiveString(sense);

    }
}
