package io.github.sashirestela.cleverclient.example;

public class MultiServiceExampleOkHttp extends MultiServiceExample {

    public MultiServiceExampleOkHttp() {
        super("okhttp");
    }

    public static void main(String[] args) {
        var example = new MultiServiceExampleOkHttp();
        example.run();
    }

}
