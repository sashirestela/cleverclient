package io.github.sashirestela.cleverclient.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Builder
@Getter
public class ResponseData {

    private final int statusCode;
    private final Object body;
    private final Map<String, List<String>> headers;
    private final Request request;

    @AllArgsConstructor
    @Builder
    @Getter
    static class Request {

        private final String httpMethod;
        private final String url;
        private final Map<String, List<String>> headers;

    }

}
