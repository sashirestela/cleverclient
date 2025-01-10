package io.github.sashirestela.cleverclient.client;

import io.github.sashirestela.cleverclient.http.HttpRequestData;
import io.github.sashirestela.cleverclient.support.ContentType;
import io.github.sashirestela.cleverclient.support.ReturnType;
import io.github.sashirestela.cleverclient.util.CommonUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.With;

import java.util.List;

@AllArgsConstructor
@Builder
@Getter
public class RequestData {

    @With
    private final String url;
    @With
    private final Object body;
    @With
    private final List<String> headers;
    private final String httpMethod;
    private final ContentType contentType;
    private final ReturnType returnType;

    public HttpRequestData getHttpRequestData() {
        return HttpRequestData.builder()
                .url(url)
                .body(body)
                .headers(CommonUtil.listToMapOfString(headers))
                .contentType(contentType)
                .httpMethod(httpMethod)
                .build();
    }

}
