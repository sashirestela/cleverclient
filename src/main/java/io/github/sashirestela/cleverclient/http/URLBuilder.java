package io.github.sashirestela.cleverclient.http;

import java.util.List;

import io.github.sashirestela.cleverclient.annotation.Path;
import io.github.sashirestela.cleverclient.annotation.Query;
import io.github.sashirestela.cleverclient.metadata.Metadata;

public class URLBuilder {

  private Metadata metadata;

  public URLBuilder(Metadata metadata) {
    this.metadata = metadata;
  }

  public String build(String methodName, Object[] arguments) {
    final var PATH = Path.class.getSimpleName();
    final var QUERY = Query.class.getSimpleName();

    var methodMetadata = metadata.getMethods().get(methodName);
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
    var first = true;
    for (var paramMetadata : paramList) {
      var index = paramMetadata.getIndex();
      var value = arguments[index];
      if (value == null) {
        continue;
      }
      var prefix = "";
      if (first) {
        prefix = "?";
        first = false;
      } else {
        prefix = "&";
      }
      var queryParam = paramMetadata.getAnnotationValue();
      var urlBuilder = new StringBuilder(url);
      urlBuilder.append(prefix);
      urlBuilder.append(queryParam);
      urlBuilder.append("=");
      urlBuilder.append(value.toString());
      url = urlBuilder.toString();
    }
    return url;
  }
}