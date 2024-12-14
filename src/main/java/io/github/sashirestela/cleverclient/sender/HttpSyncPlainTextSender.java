package io.github.sashirestela.cleverclient.sender;

import io.github.sashirestela.cleverclient.support.CleverClientException;
import io.github.sashirestela.cleverclient.support.ReturnType;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;

public class HttpSyncPlainTextSender extends HttpSender {

    @Override
    public Object sendRequest(HttpClient httpClient, HttpRequest httpRequest, ReturnType returnType) {
        try {

            var httpResponse = httpClient.send(httpRequest, BodyHandlers.ofString());

            throwExceptionIfErrorIsPresent(httpResponse, null);

            var rawData = httpResponse.body();

            logger.debug("Response : {}", rawData);

            return rawData;

        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CleverClientException(e);
        }
    }

}
