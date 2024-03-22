package io.github.sashirestela.cleverclient.sender;

import io.github.sashirestela.cleverclient.support.ReturnType;

import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;

public class HttpAsyncBinarySender extends HttpSender {

    @Override
    public Object sendRequest(HttpClient httpClient, HttpRequest httpRequest, ReturnType returnType) {

        var httpResponseFuture = httpClient.sendAsync(httpRequest, BodyHandlers.ofInputStream());

        return httpResponseFuture.thenApply(response -> {

            throwExceptionIfErrorIsPresent(response, InputStream.class);

            logger.debug("Response : {}", response.body());

            return response.body();
        });
    }

}
