package io.github.sashirestela.cleverclient.example;

import java.io.FileNotFoundException;

public class RetryExampleOkHttp extends RetryExample {

    public RetryExampleOkHttp() {
        super("okhttp");
    }

    public static void main(String[] args) throws FileNotFoundException {
        var example = new RetryExampleOkHttp();
        example.run();
    }

}
