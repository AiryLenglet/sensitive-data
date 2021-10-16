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

public static class Dto extends ParentDto {

    @Sensitive
    private String value1;
    private String value2;

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

class MyClass {

    public void doSomething() {

        SensitiveString sensitiveString = new SensitiveString("jack");

        final var dto = new Dto();
        dto.setValue2(sensitiveString.getValue()); // Noncompliant
        dto.setValue1(sensitiveString.getValue()); // compliant

    }
}
