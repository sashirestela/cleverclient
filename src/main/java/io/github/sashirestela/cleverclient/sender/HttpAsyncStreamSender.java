package io.github.sashirestela.cleverclient.sender;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;

import io.github.sashirestela.cleverclient.support.CleverClientSSE;
import io.github.sashirestela.cleverclient.util.JsonUtil;

public class HttpAsyncStreamSender extends HttpSender {

  @Override
  public <S, T> Object sendRequest(HttpClient httpClient, HttpRequest httpRequest, Class<T> responseClass,
      Class<S> genericClass) {

    var httpResponseFuture = httpClient.sendAsync(httpRequest, BodyHandlers.ofLines());

    return httpResponseFuture.thenApply(response -> {

      throwExceptionIfErrorIsPresent(response, true);

      return response.body()
          .peek(rawData -> logger.debug("Response : {}", rawData))
          .map(CleverClientSSE::new)
          .filter(CleverClientSSE::isActualData)
          .map(event -> JsonUtil.jsonToObject(event.getActualData(), responseClass));
    });
  }

}