package io.github.sashirestela.cleverclient.example;

public class BasicExampleOkHttp extends BasicExample {

    public BasicExampleOkHttp() {
        super("okhttp");
    }

    public static void main(String[] args) {
        var example = new BasicExampleOkHttp();
        example.run();
    }

}
