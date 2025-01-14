package io.github.sashirestela.cleverclient.example;

public class HeaderExampleOkHttp extends HeaderExample {

    public HeaderExampleOkHttp() {
        super("okhttp");
    }

    public static void main(String[] args) {
        var example = new HeaderExampleOkHttp();
        example.run();
    }

}
