package me.lenglet;

public class Main {

    public static void main(String... args) {

        final var dto = new Dto();
        new MyObject("jim");
        new MyObject("jim");
        new MyObject(dto.value1());
        new MyObject(dto.value2());
        new AnObject("jack");
        new AnObject("jack");
    }
}
