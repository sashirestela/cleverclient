package io.github.sashirestela.cleverclient.example;

import io.github.sashirestela.cleverclient.CleverClient;
import io.github.sashirestela.cleverclient.annotation.GET;
import io.github.sashirestela.cleverclient.annotation.Header;

public class HeaderExample extends AbstractExample {

    public HeaderExample(String clientAlias) {
        super(clientAlias);
    }

    public HeaderExample() {
        this("javahttp");
    }

    public void run() {
        var cleverClient = CleverClient.builder()
                .baseUrl("https://httpbin.org")
                .clientAdapter(clientAdapter)
                .build();

        showTitle("First Group of Headers");
        var classHeaderService = cleverClient.create(ClassHeaderService.class);
        System.out.println(classHeaderService.getFullHeaders());
        System.out.println(classHeaderService.getClassHeaders());

        showTitle("Second Group of Headers");
        var methodHeaderService = cleverClient.create(MethodHeaderService.class);
        System.out.println(methodHeaderService.getHeaders());
    }

    @Header(name = "First-Header", value = "firstValue")
    @Header(name = "Second-Header", value = "secondValue")
    static interface ClassHeaderService {

        @GET("/headers")
        @Header(name = "Third-Header", value = "thirdValue")
        String getFullHeaders();

        @GET("/headers")
        String getClassHeaders();

    }

    static interface MethodHeaderService {

        @GET("/headers")
        @Header(name = "Fourth-Header", value = "fourthValue")
        @Header(name = "Fith-Header", value = "fithValue")
        String getHeaders();

    }

    public static void main(String[] args) {
        var example = new HeaderExample();
        example.run();
    }

}
