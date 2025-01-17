package io.github.sashirestela.cleverclient.example;

public class ResponseInterceptorExampleOkHttp extends ResponseInterceptorExample {

    public ResponseInterceptorExampleOkHttp() {
        super("okhttp");
    }

    public static void main(String[] args) {
        var example = new ResponseInterceptorExampleOkHttp();
        example.run();
    }

}
