package io.github.sashirestela.cleverclient;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Represents detailed information about an HTTP response. This inner class captures comprehensive
 * details about an HTTP communication, including status code, response data, headers, and the
 * corresponding request.
 */
@AllArgsConstructor
@Builder
@Data
public class ResponseInfo implements Serializable {

    /** The HTTP status code of the response. */
    private int statusCode;

    /** The raw data received in the response. */
    private String data;

    /** The headers associated with the HTTP response. */
    private Map<String, List<String>> headers;

    /** Information about the HTTP request that generated this response. */
    private RequestInfo request;

    /**
     * Represents details of an HTTP request. This nested inner class captures essential information
     * about the HTTP request that was made, including method, URL, and headers.
     */
    @AllArgsConstructor
    @Builder
    @Data
    public static class RequestInfo implements Serializable {

        /** The HTTP method used for the request (GET, POST, etc.). */
        private String httpMethod;

        /** The full URL of the request. */
        private String url;

        /** The headers sent with the HTTP request. */
        private Map<String, List<String>> headers;

    }

}
