package io.github.sashirestela.cleverclient.sender;

import io.github.sashirestela.cleverclient.Event;
import io.github.sashirestela.cleverclient.support.CleverClientSSE;
import io.github.sashirestela.cleverclient.support.CleverClientSSE.LineRecord;
import io.github.sashirestela.cleverclient.support.ReturnType;
import io.github.sashirestela.cleverclient.util.JsonUtil;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.stream.Stream;

public class HttpAsyncStreamEventSender extends HttpSender {

    @Override
    public Object sendRequest(HttpClient httpClient, HttpRequest httpRequest, ReturnType returnType) {

        var httpResponseFuture = httpClient.sendAsync(httpRequest, BodyHandlers.ofLines());

        return httpResponseFuture.thenApply(response -> {

            throwExceptionIfErrorIsPresent(response, Stream.class);

            final var lineRecord = new LineRecord();
            final var events = returnType.getClassByEvent().keySet();

            return response.body()
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
        });
    }

}
