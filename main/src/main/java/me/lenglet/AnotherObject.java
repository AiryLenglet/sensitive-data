package me.lenglet;

public class AnotherObject {

    public AnotherObject(String value) {
        this.value = value;
    }

    @Sensitive
    private String value;

    @Override
    public String toString() {
        return "AnotherObject{" +
                "value='" + value + '\'' +
                '}';
    }
}
