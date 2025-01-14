package io.github.sashirestela.cleverclient.example;

import io.github.sashirestela.cleverclient.CleverClient;
import io.github.sashirestela.cleverclient.annotation.GET;
import io.github.sashirestela.cleverclient.annotation.Path;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileDownloadExample extends AbstractExample {

    public FileDownloadExample(String clientAlias) {
        super(clientAlias);
    }

    public FileDownloadExample() {
        this("javahttp");
    }

    public void run() throws IOException {
        var cleverClient = CleverClient.builder()
                .baseUrl("https://via.placeholder.com")
                .clientAdapter(clientAdapter)
                .build();

        var imageService = cleverClient.create(ImageService.class);
        var binaryData = imageService.getImage("92c952");
        var file = new FileOutputStream("src/test/resources/download.png");
        file.write(binaryData.readAllBytes());
        file.close();
    }

    static interface ImageService {

        @GET("/150/{id}")
        InputStream getImage(@Path("id") String id);

    }

    public static void main(String[] args) throws IOException {
        var example = new FileDownloadExample();
        example.run();
    }

}
