package io.github.sashirestela.cleverclient.example;

import io.github.sashirestela.cleverclient.client.HttpClientAdapter;
import io.github.sashirestela.cleverclient.client.JavaHttpClientAdapter;
import io.github.sashirestela.cleverclient.client.OkHttpClientAdapter;
import okhttp3.OkHttpClient;

import java.net.http.HttpClient;

public abstract class AbstractExample {

    protected HttpClientAdapter clientAdapter;

    protected AbstractExample(String clientAlias) {
        switch (clientAlias.toLowerCase()) {
            case "javahttp":
                clientAdapter = new JavaHttpClientAdapter();
                break;
            case "okhttp":
                clientAdapter = new OkHttpClientAdapter();
                break;
            default:
                clientAdapter = null;
                break;
        }
    }

    protected AbstractExample(HttpClient httpClient) {
        clientAdapter = new JavaHttpClientAdapter(httpClient);
    }

    protected AbstractExample(OkHttpClient okHttpClient) {
        clientAdapter = new OkHttpClientAdapter(okHttpClient);
    }

    protected void showTitle(String title) {
        final var times = 50;
        System.out.println("=".repeat(times));
        System.out.println(title);
        System.out.println("-".repeat(times));
    }

}
