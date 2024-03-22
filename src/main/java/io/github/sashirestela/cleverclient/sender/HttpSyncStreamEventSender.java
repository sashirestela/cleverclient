package io.github.sashirestela.cleverclient.sender;

import io.github.sashirestela.cleverclient.Event;
import io.github.sashirestela.cleverclient.support.CleverClientException;
import io.github.sashirestela.cleverclient.support.CleverClientSSE;
import io.github.sashirestela.cleverclient.support.CleverClientSSE.LineRecord;
import io.github.sashirestela.cleverclient.support.ReturnType;
import io.github.sashirestela.cleverclient.util.JsonUtil;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.stream.Stream;

public class HttpSyncStreamEventSender extends HttpSender {

    @Override
    public Object sendRequest(HttpClient httpClient, HttpRequest httpRequest, ReturnType returnType) {
        try {

            var httpResponse = httpClient.send(httpRequest, BodyHandlers.ofLines());

            throwExceptionIfErrorIsPresent(httpResponse, Stream.class);

            final var lineRecord = new LineRecord();
            final var events = returnType.getClassByEvent().keySet();

            return httpResponse.body()
                    .map(line -> {
                        logger.debug("Response : {}", line);
                        lineRecord.updateWith(line);
                        return new CleverClientSSE(lineRecord, events);
                    })
                    .filter(CleverClientSSE::isActualData)
                    .map(item -> Event.builder()
                            .name(item.getMatchedEvent())
                            .data(JsonUtil.jsonToObject(item.getActualData(),
                                    returnType.getClassByEvent().get(item.getMatchedEvent())))
                            .build());

        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CleverClientException(e.getMessage(), null, e);
        }
    }

}
