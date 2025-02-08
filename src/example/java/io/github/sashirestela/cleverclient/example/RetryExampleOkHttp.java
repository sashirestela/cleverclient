package io.github.sashirestela.cleverclient.example;

public class RetryExampleOkHttp extends RetryExample {

    public RetryExampleOkHttp() {
        super("okhttp");
    }

    public static void main(String[] args) {
        var example = new RetryExampleOkHttp();
        example.run();
    }

}
