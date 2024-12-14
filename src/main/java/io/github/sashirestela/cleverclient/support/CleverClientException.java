package io.github.sashirestela.cleverclient.support;

import io.github.sashirestela.cleverclient.util.Constant;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CleverClientException extends RuntimeException {

    private final HttpResponseInfo responseInfo;

    public CleverClientException(String message) {
        super(message);
        this.responseInfo = null;
    }

    public CleverClientException(Throwable cause) {
        super(cause);
        this.responseInfo = null;
    }

    public CleverClientException(String message, Object... parameters) {
        super(MessageFormat.format(message, Arrays.copyOfRange(parameters, 0, parameters.length - 1)),
                (Throwable) parameters[parameters.length - 1]);
        this.responseInfo = null;
    }

    public CleverClientException(HttpResponseInfo responseInfo) {
        super(MessageFormat.format(Constant.HTTP_ERROR_MESSAGE, responseInfo.getStatusCode()), null);
        this.responseInfo = responseInfo;
    }

    public Optional<HttpResponseInfo> responseInfo() {
        return Optional.ofNullable(responseInfo);
    }

    @Data
    @Builder
    public static class HttpResponseInfo implements Serializable {

        private int statusCode;
        private String data;
        private Map<String, List<String>> headers;
        private HttpRequestInfo request;

        @Data
        @Builder
        public static class HttpRequestInfo implements Serializable {

            private String httpMethod;
            private String url;
            private Map<String, List<String>> headers;

        }

    }

}
