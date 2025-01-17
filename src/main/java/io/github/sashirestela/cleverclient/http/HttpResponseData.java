package io.github.sashirestela.cleverclient.http;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
public class HttpResponseData {

    @Setter
    private String body;
    private String url;
    private String httpMethod;

}
