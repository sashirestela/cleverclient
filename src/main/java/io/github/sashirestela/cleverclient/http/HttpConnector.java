package io.github.sashirestela.cleverclient.http;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.sashirestela.cleverclient.sender.HttpSenderFactory;
import io.github.sashirestela.cleverclient.support.ContentType;
import io.github.sashirestela.cleverclient.support.HttpMultipart;
import io.github.sashirestela.cleverclient.support.ReturnType;
import io.github.sashirestela.cleverclient.util.CommonUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * HttpConnector prepares the request and receives the response to/from the
 * Java's HttpClient component.
 */
@AllArgsConstructor
@Builder
public class HttpConnector {
    private static final Logger logger = LoggerFactory.getLogger(HttpConnector.class);

    private HttpClient httpClient;
    private String url;
    private String httpMethod;
    private ReturnType returnType;
    private Object bodyObject;
    private ContentType contentType;
    private List<String> headers;
    private UnaryOperator<HttpRequestData> requestInterceptor;

    /**
     * Prepares the request to call Java's HttpClient and delegates it to a
     * specialized HttpSender based on the method's return type.
     * 
     * @return The response coming from the HttpSender's sendRequest method.
     */
    public Object sendRequest() {
        if (requestInterceptor != null) {
            interceptRequest();
        }
        logger.debug("Http Call : {} {}", httpMethod, url);
        logger.debug("Request Headers : {}", printHeaders(headers));

        var bodyPublisher = createBodyPublisher(bodyObject, contentType);
        var responseClass = returnType.getBaseClass();
        var genericClass = returnType.getGenericClassIfExists();
        var headersArray = headers.toArray(new String[0]);
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

    private void interceptRequest() {
        var httpRequestData = HttpRequestData.builder()
                .url(url)
                .body(bodyObject)
                .headers(CommonUtil.listToMapOfString(headers))
                .httpMethod(httpMethod)
                .contentType(contentType)
                .build();

        httpRequestData = requestInterceptor.apply(httpRequestData);

        url = httpRequestData.getUrl();
        bodyObject = httpRequestData.getBody();
        headers = CommonUtil.mapToListOfString(httpRequestData.getHeaders());
    }

    @SuppressWarnings("unchecked")
    private BodyPublisher createBodyPublisher(Object bodyObject, ContentType contentType) {
        BodyPublisher bodyPublisher = null;
        if (contentType == null) {
            logger.debug("Request Body : (Empty)");
            bodyPublisher = BodyPublishers.noBody();
        } else {
            switch (contentType) {
                case MULTIPART_FORMDATA:
                    logger.debug("Request Body : {}", (Map<String, Object>) bodyObject);
                    var bodyBytes = HttpMultipart.toByteArrays((Map<String, Object>) bodyObject);
                    bodyPublisher = BodyPublishers.ofByteArrays(bodyBytes);
                    break;

                case APPLICATION_JSON:
                    logger.debug("Request Body : {}", (String) bodyObject);
                    bodyPublisher = BodyPublishers.ofString((String) bodyObject);
                    break;
            }
        }
        return bodyPublisher;
    }

    private String printHeaders(List<String> headers) {
        var print = "{";
        for (var i = 0; i < headers.size(); i++) {
            if (i > 1) {
                print += ", ";
            }
            var headerKey = headers.get(i++);
            var headerVal = headerKey.equals("Authorization") ? "*".repeat(10) : headers.get(i);
            print += headerKey + " = " + headerVal;
        }
        print += "}";
        return print;
    }
}