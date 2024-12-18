package io.github.sashirestela.cleverclient.sender;

import io.github.sashirestela.cleverclient.ResponseInfo;
import io.github.sashirestela.cleverclient.ResponseInfo.RequestInfo;
import io.github.sashirestela.cleverclient.support.CleverClientException;
import io.github.sashirestela.cleverclient.support.ReturnType;
import io.github.sashirestela.cleverclient.util.CommonUtil;
import io.github.sashirestela.cleverclient.util.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * HttpSender is an abstract class for a set of concrete classes that implement different
 * interactions with the Java's HttpClient based on the method's return type.
 */
public abstract class HttpSender {

    protected static Logger logger = LoggerFactory.getLogger(HttpSender.class);

    /**
     * Method to be implementd for concrete classes to send request to the Java's HttpClient and receive
     * response.
     * 
     * @param httpClient  Java's HttpClient component.
     * @param httpRequest Java's HttpRequest component.
     * @param returnType  Response class and generic class if exists.
     * @return Response coming from Java's HttpClient.
     */
    public abstract Object sendRequest(HttpClient httpClient, HttpRequest httpRequest, ReturnType returnType);

    /**
     * Exception handling that will be called by any concrete class.
     * 
     * @param response Java's HttpResponse component.
     * @param clazz    Response class.
     */
    @SuppressWarnings("unchecked")
    protected void throwExceptionIfErrorIsPresent(HttpResponse<?> response, Class<?> clazz) {
        logger.debug("Response Code : {}", response.statusCode());
        if (CommonUtil.isBetweenHundredsOf(response.statusCode(), Constant.HTTP_CLIENT_ERROR_CODE,
                Constant.HTTP_SERVER_ERROR_CODE)) {
            var data = "";
            if (Stream.class.equals(clazz)) {
                data = ((Stream<String>) response.body())
                        .collect(Collectors.joining(System.getProperty("line.separator")));
            } else if (InputStream.class.equals(clazz)) {
                try {
                    data = new String(((InputStream) response.body()).readAllBytes(), StandardCharsets.UTF_8);
                } catch (IOException e) {
                    throw new CleverClientException(e);
                }
            } else {
                data = (String) response.body();
            }
            logger.error("Response : {}", data);
            throw new CleverClientException(fillResponseInfo(response, data));
        }
    }

    private ResponseInfo fillResponseInfo(HttpResponse<?> response, String data) {
        var request = response.request();
        return ResponseInfo.builder()
                .statusCode(response.statusCode())
                .data(data)
                .headers(response.headers().map())
                .request(RequestInfo.builder()
                        .httpMethod(request.method())
                        .url(request.uri().toString())
                        .headers(request.headers().map())
                        .build())
                .build();
    }

}
