package io.github.sashirestela.cleverclient.sender;

import io.github.sashirestela.cleverclient.support.CleverClientException;
import io.github.sashirestela.cleverclient.support.CleverClientSSE;
import io.github.sashirestela.cleverclient.util.JsonUtil;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.stream.Stream;

public class HttpSyncStreamSender extends HttpSender {

    @Override
    public <S, T> Object sendRequest(HttpClient httpClient, HttpRequest httpRequest, Class<T> responseClass,
            Class<S> genericClass) {
        try {

            var httpResponse = httpClient.send(httpRequest, BodyHandlers.ofLines());

            throwExceptionIfErrorIsPresent(httpResponse, Stream.class);

            final var lineRecord = new CleverClientSSE.LineRecord();

            return httpResponse.body()
                    .peek(line -> {
                        logger.debug("Response : {}", line);
                        lineRecord.updateWith(line);
                    })
                    .map(line -> new CleverClientSSE(lineRecord))
                    .filter(CleverClientSSE::isActualData)
                    .map(item -> JsonUtil.jsonToObject(item.getActualData(), responseClass));

        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CleverClientException(e.getMessage(), null, e);
        }
    }

}
