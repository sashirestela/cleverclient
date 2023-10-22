package io.github.sashirestela.cleverclient.sender;

import java.net.HttpURLConnection;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.sashirestela.cleverclient.support.CleverClientException;
import io.github.sashirestela.cleverclient.util.CommonUtil;

public abstract class HttpSender {
  protected static Logger logger = LoggerFactory.getLogger(HttpSender.class);

  public abstract <S, T> Object sendRequest(HttpClient httpClient, HttpRequest httpRequest, Class<T> responseClass,
      Class<S> genericClass);

  @SuppressWarnings("unchecked")
  protected void throwExceptionIfErrorIsPresent(HttpResponse<?> response, boolean isStream) {
    logger.debug("Response Code : {}", response.statusCode());
    if (!CommonUtil.isInHundredsOf(response.statusCode(), HttpURLConnection.HTTP_OK)) {
      var data = "";
      if (isStream) {
        data = ((Stream<String>) response.body()).collect(Collectors.joining(System.getProperty("line.separator")));
      } else {
        data = (String) response.body();
      }
      logger.error("Response : {}", data);
      throw new CleverClientException("ERROR : {0}", data, null);
    }
  }
}