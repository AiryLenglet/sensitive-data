package me.lenglet;

public class Dto {

    @Sensitive
    private String value1;
    private String value2;

    public String value1() {
        return this.value1;
    }

    public String value2() {
        return this.value2;
    }
}
