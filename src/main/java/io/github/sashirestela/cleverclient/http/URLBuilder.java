package io.github.sashirestela.cleverclient.http;

import io.github.sashirestela.cleverclient.metadata.InterfaceMetadata.MethodMetadata;
import io.github.sashirestela.cleverclient.metadata.InterfaceMetadata.ParameterMetadata;
import io.github.sashirestela.cleverclient.util.JsonUtil;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class URLBuilder {

    private static URLBuilder urlBuilder = null;

    private URLBuilder() {
    }

    public static URLBuilder one() {
        if (urlBuilder == null) {
            urlBuilder = new URLBuilder();
        }
        return urlBuilder;
    }

    public String build(String urlMethod, MethodMetadata methodMetadata, Object[] arguments) {
        var url = urlMethod;
        var pathParameters = methodMetadata.getPathParameters();
        var queryParameters = methodMetadata.getQueryParameters();
        if (pathParameters.isEmpty() && queryParameters.isEmpty()) {
            return url;
        }
        url = replacePathParams(url, pathParameters, arguments);
        url = includeQueryParams(url, queryParameters, arguments);
        return url;
    }

    private String replacePathParams(String url, List<ParameterMetadata> pathParameters, Object[] arguments) {
        for (var parameter : pathParameters) {
            var index = parameter.getIndex();
            var pathParam = "{" + parameter.getAnnotation().getValue() + "}";
            url = url.replace(pathParam, arguments[index].toString());
        }
        return url;
    }

    private String includeQueryParams(String url, List<ParameterMetadata> queryParameters, Object[] arguments) {
        var queryParamBuilder = new StringBuilder();
        for (var parameter : queryParameters) {
            var index = parameter.getIndex();
            var value = arguments[index];
            if (value == null) {
                continue;
            }
            var queryParam = parameter.getAnnotation().getValue();
            if (queryParam == null || queryParam.isEmpty())
                appendQueryParams(JsonUtil.objectToMap(value), queryParamBuilder);
            else
                appendQueryParam(queryParam, value, queryParamBuilder);
        }
        return url + queryParamBuilder;
    }

    private void appendQueryParams(Map<String, ?> queryParams, StringBuilder queryParamBuilder) {
        queryParams.forEach((k, v) -> appendQueryParam(k, v, queryParamBuilder));
    }

    private void appendQueryParam(String name, Object value, StringBuilder queryParamBuilder) {
        if (value != null) {
            queryParamBuilder.append(queryParamBuilder.length() == 0 ? '?' : '&')
                    .append(URLEncoder.encode(name, StandardCharsets.UTF_8))
                    .append('=')
                    .append(URLEncoder.encode(value.toString(), StandardCharsets.UTF_8));
        }
    }

}
