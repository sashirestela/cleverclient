package io.github.sashirestela.cleverclient.client;

import io.github.sashirestela.cleverclient.ResponseInfo;
import io.github.sashirestela.cleverclient.ResponseInfo.RequestInfo;
import io.github.sashirestela.cleverclient.http.HttpRequestData;
import io.github.sashirestela.cleverclient.support.CleverClientException;
import io.github.sashirestela.cleverclient.util.CommonUtil;
import io.github.sashirestela.cleverclient.util.Constant;
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

public abstract class HttpClientAdapter {

    private static Logger logger = LoggerFactory.getLogger(HttpClientAdapter.class);
    protected static final String REQUEST_BODY_FORMAT = "Request Body : {}";
    protected static final String RESPONSE_CODE_FORMAT = "Response Code : {}";
    protected static final String RESPONSE_FORMAT = "Response : {}";

    public Object sendRequest(RequestData originalRequest, UnaryOperator<HttpRequestData> requestInterceptor) {
        var actualRequest = interceptRequest(originalRequest, requestInterceptor);
        logger.debug("Http Call : {} {}", actualRequest.getHttpMethod(), actualRequest.getUrl());
        var formattedHeaders = formattedHeaders(actualRequest.getHeaders());
        logger.debug("Request Headers : {}", formattedHeaders);
        if (actualRequest.getReturnType().isAsync()) {
            return sendAsync(actualRequest);
        } else {
            return send(actualRequest);
        }
    }

    protected abstract Object sendAsync(RequestData request);

    protected abstract Object send(RequestData request);

    private RequestData interceptRequest(RequestData originalRequest,
            UnaryOperator<HttpRequestData> requestInterceptor) {
        if (requestInterceptor != null) {
            var httpRequestData = originalRequest.getHttpRequestData();
            httpRequestData = requestInterceptor.apply(httpRequestData);
            return originalRequest
                    .withUrl(httpRequestData.getUrl())
                    .withBody(httpRequestData.getBody())
                    .withHeaders(CommonUtil.mapToListOfString(httpRequestData.getHeaders()));
        } else {
            return originalRequest;
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

}
