package io.github.sashirestela.cleverclient.example;

public class MultipartExampleOkHttp extends MultipartExample {

    public MultipartExampleOkHttp() {
        super("okhttp");
    }

    public static void main(String[] args) {
        var example = new MultipartExampleOkHttp();
        example.run();
    }

}
