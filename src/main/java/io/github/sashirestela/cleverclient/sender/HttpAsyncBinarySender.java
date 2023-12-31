package io.github.sashirestela.cleverclient.sender;

import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;

public class HttpAsyncBinarySender extends HttpSender {

    @Override
    @SuppressWarnings("unchecked")
    public <S, T> Object sendRequest(HttpClient httpClient, HttpRequest httpRequest, Class<T> responseClass,
            Class<S> genericClass) {

        var httpResponseFuture = httpClient.sendAsync(httpRequest, BodyHandlers.ofInputStream());

        return httpResponseFuture.thenApply(response -> {

            throwExceptionIfErrorIsPresent(response, InputStream.class);

            logger.debug("Response : {}", response.body());

            return (T) response.body();
        });
    }

}