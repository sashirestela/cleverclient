package io.github.sashirestela.cleverclient.example;

import java.io.IOException;

public class FileDownloadExampleOkHttp extends FileDownloadExample {

    public FileDownloadExampleOkHttp() {
        super("okhttp");
    }

    public static void main(String[] args) throws IOException {
        var example = new FileDownloadExampleOkHttp();
        example.run();
    }

}
