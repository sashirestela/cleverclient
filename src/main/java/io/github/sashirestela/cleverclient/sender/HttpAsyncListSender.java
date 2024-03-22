package io.github.sashirestela.cleverclient.sender;

import io.github.sashirestela.cleverclient.support.ReturnType;
import io.github.sashirestela.cleverclient.util.JsonUtil;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;

public class HttpAsyncListSender extends HttpSender {

    @Override
    public Object sendRequest(HttpClient httpClient, HttpRequest httpRequest, ReturnType returnType) {

        var httpResponseFuture = httpClient.sendAsync(httpRequest, BodyHandlers.ofString());

        return httpResponseFuture.thenApply(response -> {

            throwExceptionIfErrorIsPresent(response, null);

            logger.debug("Response : {}", response.body());

            return JsonUtil.jsonToList(response.body(), returnType.getBaseClass());
        });
    }

}
