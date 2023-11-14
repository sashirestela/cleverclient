package io.github.sashirestela.cleverclient.sender;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;

import io.github.sashirestela.cleverclient.support.CleverClientException;

public class HttpSyncPlainTextSender extends HttpSender {

    @Override
    public <S, T> Object sendRequest(HttpClient httpClient, HttpRequest httpRequest, Class<T> responseClass,
            Class<S> genericClass) {
        try {

            var httpResponse = httpClient.send(httpRequest, BodyHandlers.ofString());

            throwExceptionIfErrorIsPresent(httpResponse, null);

            var rawData = httpResponse.body();

            logger.debug("Response : {}", rawData);

            return rawData;

        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CleverClientException(e.getMessage(), null, e);
        }
    }

}