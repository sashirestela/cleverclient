package io.github.sashirestela.cleverclient.client;

import io.github.sashirestela.cleverclient.http.HttpResponseData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.With;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Builder
@Getter
public class ResponseData {

    private final int statusCode;
    @With
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

    public HttpResponseData getHttpResponseData() {
        return HttpResponseData.builder()
                .body((String) body)
                .url(request.url)
                .httpMethod(request.httpMethod)
                .build();
    }

    public HttpResponseData getHttpResponseData(String body) {
        return HttpResponseData.builder()
                .body(body)
                .url(request.url)
                .httpMethod(request.httpMethod)
                .build();
    }

}
