package io.github.sashirestela.cleverclient.http;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.sashirestela.cleverclient.sender.HttpSenderFactory;
import io.github.sashirestela.cleverclient.support.HttpMultipart;
import io.github.sashirestela.cleverclient.support.ReturnType;
import io.github.sashirestela.cleverclient.util.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * HttpConnector prepares the request and receives the response to/from the
 * Java's HttpClient component.
 */
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

    /**
     * Prepares the request to call Java's HttpClient and delegates it to a
     * specialized HttpSender based on the method's return type.
     * 
     * @return The response coming from the HttpSender's sendRequest method.
     */
    public Object sendRequest() {
        var bodyPublisher = createBodyPublisher(bodyObject, isMultipart);
        var responseClass = returnType.getBaseClass();
        var genericClass = returnType.getGenericClassIfExists();
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
        return httpSender.sendRequest(httpClient, httpRequest, responseClass, genericClass);
    }

    private BodyPublisher createBodyPublisher(Object bodyObject, boolean isMultipart) {
        BodyPublisher bodyPublisher = null;
        if (bodyObject == null) {
            logger.debug("Request Body : (Empty)");
            bodyPublisher = BodyPublishers.noBody();
        } else if (isMultipart) {
            var data = JsonUtil.objectToMap(bodyObject);
            var requestBytes = HttpMultipart.toByteArrays(data);
            logger.debug("Request Body : {}", data);
            bodyPublisher = BodyPublishers.ofByteArrays(requestBytes);
        } else {
            var requestString = JsonUtil.objectToJson(bodyObject);
            logger.debug("Request Body : {}", requestString);
            bodyPublisher = BodyPublishers.ofString(requestString);
        }
        return bodyPublisher;
    }
}