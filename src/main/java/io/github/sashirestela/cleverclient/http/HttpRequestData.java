package io.github.sashirestela.cleverclient.http;

import java.util.Map;

import io.github.sashirestela.cleverclient.support.ContentType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
public class HttpRequestData {
    @Setter private String url;
    @Setter private Object body;
    @Setter private Map<String, String> headers;
    private String httpMethod;
    private ContentType contentType;
}
