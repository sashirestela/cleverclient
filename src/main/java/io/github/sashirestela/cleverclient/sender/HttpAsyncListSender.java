package io.github.sashirestela.cleverclient.sender;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;

import io.github.sashirestela.cleverclient.util.JsonUtil;

public class HttpAsyncListSender extends HttpSender {

    @Override
    public <S, T> Object sendRequest(HttpClient httpClient, HttpRequest httpRequest, Class<T> responseClass,
            Class<S> genericClass) {

        var httpResponseFuture = httpClient.sendAsync(httpRequest, BodyHandlers.ofString());

        return httpResponseFuture.thenApply(response -> {

            throwExceptionIfErrorIsPresent(response, null);

            logger.debug("Response : {}", response.body());

            return JsonUtil.jsonToList(response.body(), responseClass);
        });
    }

}