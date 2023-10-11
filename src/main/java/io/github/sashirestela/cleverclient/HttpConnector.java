package io.github.sashirestela.cleverclient;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.sashirestela.cleverclient.sender.HttpSenderFactory;
import io.github.sashirestela.cleverclient.util.JsonUtil;
import io.github.sashirestela.cleverclient.util.ReflectUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;

@AllArgsConstructor
@Builder
public class HttpConnector {
  private static Logger logger = LoggerFactory.getLogger(HttpConnector.class);

  private HttpClient httpClient;
  private String url;
  private String httpMethod;
  private ReturnType returnType;
  private Object bodyObject;
  private boolean isMultipart;
  private String[] headersArray;

  public Object sendRequest() {
    var bodyPublisher = createBodyPublisher(bodyObject, isMultipart);
    var responseClass = returnType.getBaseClass();
    var httpRequest = HttpRequest.newBuilder()
        .uri(URI.create(url))
        .headers(headersArray)
        .method(httpMethod, bodyPublisher)
        .build();
    var httpSender = HttpSenderFactory.get().createSender(returnType);
    return httpSender.sendRequest(httpClient, httpRequest, responseClass);
  }

  private BodyPublisher createBodyPublisher(Object bodyObject, boolean isMultipart) {
    BodyPublisher bodyPublisher = null;
    if (bodyObject == null) {
      logger.debug("Body Request: (Empty)");
      bodyPublisher = BodyPublishers.noBody();
    } else if (isMultipart) {
      var data = ReflectUtil.get().getMapFields(bodyObject);
      var requestBytes = HttpMultipart.get().toByteArrays(data);
      logger.debug("Body Request: {}", data);
      bodyPublisher = BodyPublishers.ofByteArrays(requestBytes);
    } else {
      var requestString = JsonUtil.get().objectToJson(bodyObject);
      logger.debug("Body Request: {}", requestString);
      bodyPublisher = BodyPublishers.ofString(requestString);
    }
    return bodyPublisher;
  }
}