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

/**
 * A custom exception class for handling HTTP client-related errors and providing detailed
 * information about HTTP responses and requests. This exception extends RuntimeException and can be
 * used to capture and represent various HTTP communication errors with additional context.
 */
public class CleverClientException extends RuntimeException {

    /**
     * Stores HTTP response information associated with this exception.
     */
    private final HttpResponseInfo responseInfo;

    /**
     * Constructs a new CleverClientException with a simple error message.
     *
     * @param message A descriptive error message
     */
    public CleverClientException(String message) {
        super(message);
        this.responseInfo = null;
    }

    /**
     * Constructs a new CleverClientException with a cause.
     *
     * @param cause The original exception that triggered this CleverClientException
     */
    public CleverClientException(Throwable cause) {
        super(cause);
        this.responseInfo = null;
    }

    /**
     * Constructs a new CleverClientException with a formatted message and a cause.
     *
     * @param message    A message template with placeholders for dynamic values
     * @param parameters An array of parameters to format the message, with the last parameter being the
     *                   original cause
     */
    public CleverClientException(String message, Object... parameters) {
        super(MessageFormat.format(message, Arrays.copyOfRange(parameters, 0, parameters.length - 1)),
                (Throwable) parameters[parameters.length - 1]);
        this.responseInfo = null;
    }

    /**
     * Constructs a new CleverClientException with HTTP response information.
     *
     * @param responseInfo Detailed information about the HTTP response
     */
    public CleverClientException(HttpResponseInfo responseInfo) {
        super(MessageFormat.format(Constant.HTTP_ERROR_MESSAGE, responseInfo.getStatusCode()), null);
        this.responseInfo = responseInfo;
    }

    /**
     * Retrieves the associated HTTP response information as an Optional.
     *
     * @return An Optional containing HttpResponseInfo if available, otherwise empty
     */
    public Optional<HttpResponseInfo> responseInfo() {
        return Optional.ofNullable(responseInfo);
    }

    /**
     * Attempts to extract a CleverClientException from a given Throwable. This method checks if the
     * exception or its cause is a CleverClientException.
     *
     * @param exception The throwable to extract the CleverClientException from
     * @return An Optional containing the CleverClientException if found
     */
    public static Optional<CleverClientException> getFrom(Throwable exception) {
        if (exception instanceof CleverClientException) {
            return Optional.of((CleverClientException) exception);
        } else if (exception.getCause() instanceof CleverClientException) {
            return Optional.of((CleverClientException) exception.getCause());
        } else {
            return Optional.empty();
        }
    }

    /**
     * Represents detailed information about an HTTP response. This inner class captures comprehensive
     * details about an HTTP communication, including status code, response data, headers, and the
     * corresponding request.
     */
    @Data
    @Builder
    public static class HttpResponseInfo implements Serializable {

        /** The HTTP status code of the response. */
        private int statusCode;

        /** The raw data received in the response. */
        private String data;

        /** The headers associated with the HTTP response. */
        private Map<String, List<String>> headers;

        /** Information about the HTTP request that generated this response. */
        private HttpRequestInfo request;

        /**
         * Represents details of an HTTP request. This nested inner class captures essential information
         * about the HTTP request that was made, including method, URL, and headers.
         */
        @Data
        @Builder
        public static class HttpRequestInfo implements Serializable {

            /** The HTTP method used for the request (GET, POST, etc.). */
            private String httpMethod;

            /** The full URL of the request. */
            private String url;

            /** The headers sent with the HTTP request. */
            private Map<String, List<String>> headers;

        }

    }

}
