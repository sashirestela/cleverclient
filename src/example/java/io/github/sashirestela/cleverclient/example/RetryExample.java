package io.github.sashirestela.cleverclient.example;

import io.github.sashirestela.cleverclient.CleverClient;
import io.github.sashirestela.cleverclient.annotation.POST;
import io.github.sashirestela.cleverclient.annotation.Resource;
import io.github.sashirestela.cleverclient.retry.RetryConfig;

import java.util.concurrent.CompletableFuture;

public class RetryExample extends AbstractExample {

    public RetryExample(String clientAlias) {
        super(clientAlias);
    }

    public RetryExample() {
        this("javahttp");
    }

    public void run() {
        var cleverClient = CleverClient.builder()
                .baseUrl("https://cleverclient.free.beeceptor.com")
                .clientAdapter(clientAdapter)
                .retryConfig(RetryConfig.of())
                .build();
        var service = cleverClient.create(BadService.class);

        showTitle("Retrying Bad Service");
        try {
            var response = service.create();
            System.out.println(response);
        } catch (Exception e) {
            e.printStackTrace();
        }

        showTitle("Retrying Async Bad Service");
        try {
            var asyncResponse = service.createAsync().join();
            System.out.println(asyncResponse);
        } catch (Exception e) {
            e.printStackTrace();
        }

        clientAdapter.shutdown();

    }

    @Resource("/badservice")
    static interface BadService {

        @POST
        String create();

        @POST
        CompletableFuture<String> createAsync();

    }

    public static void main(String[] args) {
        var example = new RetryExample();
        example.run();
    }

}
