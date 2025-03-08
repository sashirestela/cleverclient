package io.github.sashirestela.cleverclient.example;

import io.github.sashirestela.cleverclient.CleverClient;
import io.github.sashirestela.cleverclient.annotation.POST;
import io.github.sashirestela.cleverclient.annotation.Resource;
import io.github.sashirestela.cleverclient.example.util.Commons;
import io.github.sashirestela.cleverclient.example.util.Commons.MyExceptionConverter;
import io.github.sashirestela.cleverclient.example.util.Commons.MyHttpException;
import io.github.sashirestela.cleverclient.retry.RetryConfig;

import java.io.FileNotFoundException;
import java.util.concurrent.CompletableFuture;

public class RetryExample extends AbstractExample {

    public RetryExample(String clientAlias) {
        super(clientAlias);
    }

    public RetryExample() {
        this("javahttp");
    }

    public void run() throws FileNotFoundException {
        Commons.redirectSystemErr();
        var cleverClient = CleverClient.builder()
                .baseUrl("https://cleverclient.free.beeceptor.com")
                .clientAdapter(clientAdapter)
                .retryConfig(RetryConfig.defaultValues())
                .build();
        var service = cleverClient.create(BadService.class);

        showTitle("Retrying Bad Service");
        try {
            var response = service.create();
            System.out.println(response);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                MyExceptionConverter.rethrow(e);
            } catch (MyHttpException mhe) {
                System.out.println("Http Response Code: " + mhe.getResponseCode() +
                        "\nError Detail:\n" + mhe.getErrorDetail());
            } catch (RuntimeException re) {
                System.out.println(re.getMessage());
            }
        }

        showTitle("Retrying Async Bad Service");
        try {
            var asyncResponse = service.createAsync().join();
            System.out.println(asyncResponse);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                MyExceptionConverter.rethrow(e);
            } catch (MyHttpException mhe) {
                System.out.println("Http Response Code: " + mhe.getResponseCode() +
                        "\nError Detail:\n" + mhe.getErrorDetail());
            } catch (RuntimeException re) {
                System.out.println(re.getMessage());
            }
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

    public static void main(String[] args) throws FileNotFoundException {
        var example = new RetryExample();
        example.run();
    }

}
