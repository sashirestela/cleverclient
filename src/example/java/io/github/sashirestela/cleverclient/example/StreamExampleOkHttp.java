package io.github.sashirestela.cleverclient.example;

public class StreamExampleOkHttp extends StreamExample {

    public StreamExampleOkHttp() {
        super("okhttp");
    }

    public static void main(String[] args) {
        var example = new StreamExampleOkHttp();
        example.run();
    }

}
