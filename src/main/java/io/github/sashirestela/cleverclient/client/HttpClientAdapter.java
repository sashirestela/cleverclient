package io.github.sashirestela.cleverclient.client;

import io.github.sashirestela.cleverclient.Event;
import io.github.sashirestela.cleverclient.ResponseInfo;
import io.github.sashirestela.cleverclient.ResponseInfo.RequestInfo;
import io.github.sashirestela.cleverclient.http.HttpRequestData;
import io.github.sashirestela.cleverclient.http.HttpResponseData;
import io.github.sashirestela.cleverclient.retry.RetryableRequest;
import io.github.sashirestela.cleverclient.support.CleverClientException;
import io.github.sashirestela.cleverclient.support.CleverClientSSE;
import io.github.sashirestela.cleverclient.support.ReturnType;
import io.github.sashirestela.cleverclient.util.CommonUtil;
import io.github.sashirestela.cleverclient.util.Constant;
import io.github.sashirestela.cleverclient.util.JsonUtil;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Setter
public abstract class HttpClientAdapter {

    private static Logger logger = LoggerFactory.getLogger(HttpClientAdapter.class);
    protected static final String REQUEST_BODY_FORMAT = "Request Body : {}";
    protected static final String RESPONSE_CODE_FORMAT = "Response Code : {}";
    protected static final String RESPONSE_FORMAT = "Response : {}";

    protected UnaryOperator<HttpRequestData> requestInterceptor;
    protected UnaryOperator<HttpResponseData> responseInterceptor;
    protected RetryableRequest retryableRequest;

    public Object sendRequest(RequestData originalRequest) {
        var actualRequest = interceptRequest(originalRequest);
        logger.debug("Http Call : {} {}", actualRequest.getHttpMethod(), actualRequest.getUrl());
        var formattedHeaders = formattedHeaders(actualRequest.getHeaders());
        logger.debug("Request Headers : {}", formattedHeaders);
        if (retryableRequest == null) {
            if (actualRequest.getReturnType().isAsync()) {
                return sendAsync(actualRequest);
            } else {
                return send(actualRequest);
            }
        } else {
            if (actualRequest.getReturnType().isAsync()) {
                return retryableRequest.executeAsync(() -> sendAsync(actualRequest));
            } else {
                return retryableRequest.execute(() -> send(actualRequest));
            }
        }
    }

    protected abstract Object sendAsync(RequestData request);

    protected abstract Object send(RequestData request);

    public abstract void shutdown();

    private RequestData interceptRequest(RequestData originalRequest) {
        if (requestInterceptor != null) {
            var httpRequestData = originalRequest.getHttpRequestData();
            httpRequestData = this.requestInterceptor.apply(httpRequestData);
            return originalRequest
                    .withUrl(httpRequestData.getUrl())
                    .withBody(httpRequestData.getBody())
                    .withHeaders(CommonUtil.mapToListOfString(httpRequestData.getHeaders()));
        } else {
            return originalRequest;
        }
    }

    protected ResponseData interceptResponse(ResponseData originalResponse) {
        if (responseInterceptor != null && originalResponse.getBody() instanceof String) {
            var httpResponseData = originalResponse.getHttpResponseData();
            httpResponseData = this.responseInterceptor.apply(httpResponseData);
            return originalResponse.withBody(httpResponseData.getBody());
        } else {
            return originalResponse;
        }
    }

    @SuppressWarnings("unchecked")
    protected void throwExceptionIfErrorIsPresent(ResponseData response) {
        if (!CommonUtil.isInHundredsOf(response.getStatusCode(), Constant.HTTP_SUCCESSFUL)) {
            String data = "";
            if (response.getBody() instanceof Stream) {
                data = ((Stream<String>) response.getBody())
                        .collect(Collectors.joining(System.getProperty("line.separator")));
            } else if (response.getBody() instanceof InputStream) {
                try {
                    data = new String(((InputStream) response.getBody()).readAllBytes(), StandardCharsets.UTF_8);
                } catch (IOException e) {
                    throw new CleverClientException(e);
                }
            } else {
                data = (String) response.getBody();
            }
            logger.error(RESPONSE_FORMAT, data);
            throw new CleverClientException(fillResponseInfo(response, data));
        }
    }

    private ResponseInfo fillResponseInfo(ResponseData response, String data) {
        var request = response.getRequest();
        return ResponseInfo.builder()
                .statusCode(response.getStatusCode())
                .data(data)
                .headers(response.getHeaders())
                .request(request != null ? RequestInfo.builder()
                        .httpMethod(request.getHttpMethod())
                        .url(request.getUrl())
                        .headers(request.getHeaders())
                        .build() : null)
                .build();
    }

    private String formattedHeaders(List<String> headers) {
        final String RESERVED_REGEX = "(authorization|api.?key)";
        var pattern = Pattern.compile(RESERVED_REGEX, Pattern.CASE_INSENSITIVE);
        var print = new StringBuilder("{");
        for (var i = 0; i < headers.size(); i += 2) {
            if (i > 1) {
                print.append(", ");
            }
            var headerKey = headers.get(i);
            var matcher = pattern.matcher(headerKey);
            var headerVal = matcher.find() ? "*".repeat(10) : headers.get(i + 1);
            print.append(headerKey + " = " + headerVal);
        }
        print.append("}");
        return print.toString();
    }

    @SuppressWarnings("unchecked")
    protected Stream<Object> convertToStreamOfObjects(ResponseData responseData, ReturnType returnType) {
        final var lineRecord = new CleverClientSSE.LineRecord();
        return ((Stream<String>) responseData.getBody())
                .map(line -> {
                    logger.debug(RESPONSE_FORMAT, line);
                    lineRecord.updateWith(line);
                    return new CleverClientSSE(lineRecord);
                })
                .filter(CleverClientSSE::isActualData)
                .map(item -> JsonUtil.jsonToObject(interceptStreamItem(responseData, item.getActualData()),
                        returnType.getBaseClass()));
    }

    @SuppressWarnings("unchecked")
    protected Stream<Object> convertToStreamOfEvents(ResponseData responseData, ReturnType returnType) {
        final var lineRecord = new CleverClientSSE.LineRecord();
        final var events = returnType.getClassByEvent().keySet();
        return ((Stream<String>) responseData.getBody())
                .map(line -> {
                    logger.debug(RESPONSE_FORMAT, line);
                    lineRecord.updateWith(line);
                    return new CleverClientSSE(lineRecord, events);
                })
                .filter(CleverClientSSE::isActualData)
                .map(item -> Event.builder()
                        .name(item.getMatchedEvent())
                        .data(JsonUtil.jsonToObject(interceptStreamItem(responseData, item.getActualData()),
                                returnType.getClassByEvent().get(item.getMatchedEvent())))
                        .build());
    }

    private String interceptStreamItem(ResponseData responseData, String text) {
        if (this.responseInterceptor == null) {
            return text;
        }
        var httpResponseData = responseData.getHttpResponseData(text);
        httpResponseData = this.responseInterceptor.apply(httpResponseData);
        return httpResponseData.getBody();
    }

}
