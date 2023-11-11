package io.github.sashirestela.cleverclient.http;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import io.github.sashirestela.cleverclient.annotation.Path;
import io.github.sashirestela.cleverclient.annotation.Query;
import io.github.sashirestela.cleverclient.metadata.Metadata;
import io.github.sashirestela.cleverclient.metadata.MethodSignature;
import io.github.sashirestela.cleverclient.util.JsonUtil;

public class URLBuilder {

    private final Metadata metadata;

    public URLBuilder(Metadata metadata) {
        this.metadata = metadata;
    }

    public String build(MethodSignature method, Object[] arguments) {
        final var PATH = Path.class.getSimpleName();
        final var QUERY = Query.class.getSimpleName();

        var methodMetadata = metadata.getMethods().get(method);
        var url = methodMetadata.getUrl();
        var pathParamList = methodMetadata.getParametersByType().get(PATH);
        var queryParamList = methodMetadata.getParametersByType().get(QUERY);
        if (pathParamList.isEmpty() && queryParamList.isEmpty()) {
            return url;
        }
        url = replacePathParams(url, pathParamList, arguments);
        url = includeQueryParams(url, queryParamList, arguments);
        return url;
    }

    private String replacePathParams(String url, List<Metadata.Parameter> paramList, Object[] arguments) {
        for (var paramMetadata : paramList) {
            var index = paramMetadata.getIndex();
            var pathParam = "{" + paramMetadata.getAnnotationValue() + "}";
            url = url.replace(pathParam, arguments[index].toString());
        }
        return url;
    }

    private String includeQueryParams(String url, List<Metadata.Parameter> paramList, Object[] arguments) {
        var queryParamBuilder = new StringBuilder();
        for (var paramMetadata : paramList) {
            var index = paramMetadata.getIndex();
            var value = arguments[index];
            if (value == null) {
                continue;
            }
            var queryParam = paramMetadata.getAnnotationValue();
            if (queryParam == null || queryParam.isEmpty())
                appendQueryParams(JsonUtil.objectToMap(value), queryParamBuilder);
            else
                appendQueryParam(queryParam, value, queryParamBuilder);
        }
        return url + queryParamBuilder;
    }

    protected void appendQueryParams(Map<String, ?> queryParams, StringBuilder queryParamBuilder) {
        queryParams.forEach((k, v) -> appendQueryParam(k, v, queryParamBuilder));
    }

    protected void appendQueryParam(String name, Object value, StringBuilder queryParamBuilder) {
        if (value != null) {
            queryParamBuilder.append(queryParamBuilder.length() == 0 ? '?' : '&')
                    .append(URLEncoder.encode(name, StandardCharsets.UTF_8))
                    .append('=')
                    .append(URLEncoder.encode(value.toString(), StandardCharsets.UTF_8));
        }
    }
}