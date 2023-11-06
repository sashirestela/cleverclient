package io.github.sashirestela.cleverclient.http;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.util.List;

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
    private List<Object> bodyObjects;
    private boolean isMultipart;
    private String[] headersArray;

    public Object sendRequest() {
        var bodyPublisher = createBodyPublisher(bodyObjects, isMultipart);
        var responseClass = returnType.getBaseClass();
        HttpRequest httpRequest = null;
        if (headersArray.length > 0) {
            httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .headers(headersArray)
                    .method(httpMethod, bodyPublisher)
                    .build();
        } else {
            httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .method(httpMethod, bodyPublisher)
                    .build();
        }
        var httpSender = HttpSenderFactory.get().createSender(returnType);
        if (returnType.isGeneric()) {
            return httpSender.sendRequest(httpClient, httpRequest, responseClass, returnType.getGenericClass());
        } else {
            return httpSender.sendRequest(httpClient, httpRequest, responseClass, null);
        }
    }

    private BodyPublisher createBodyPublisher(List<Object> bodyObjects, boolean isMultipart) {
        BodyPublisher bodyPublisher = null;
        if (bodyObjects.isEmpty()) {
            logger.debug("Body Request: (Empty)");
            bodyPublisher = BodyPublishers.noBody();
        } else if (isMultipart) {
            var data = ReflectUtil.getMapFields(bodyObjects);
            var requestBytes = HttpMultipart.toByteArrays(data);
            logger.debug("Body Request: {}", data);
            bodyPublisher = BodyPublishers.ofByteArrays(requestBytes);
        } else {
            var requestString = JsonUtil.objectsToJson(bodyObjects);
            logger.debug("Body Request: {}", requestString);
            bodyPublisher = BodyPublishers.ofString(requestString);
        }
        return bodyPublisher;
    }
}