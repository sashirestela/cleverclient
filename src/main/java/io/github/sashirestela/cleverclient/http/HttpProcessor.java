package io.github.sashirestela.cleverclient.http;

import java.lang.reflect.Method;
import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.sashirestela.cleverclient.annotation.Body;
import io.github.sashirestela.cleverclient.annotation.Path;
import io.github.sashirestela.cleverclient.metadata.Metadata;
import io.github.sashirestela.cleverclient.metadata.MetadataCollector;
import io.github.sashirestela.cleverclient.support.CleverClientException;
import io.github.sashirestela.cleverclient.util.CommonUtil;
import io.github.sashirestela.cleverclient.util.Constant;
import io.github.sashirestela.cleverclient.util.ReflectUtil;

public class HttpProcessor {
  private static Logger logger = LoggerFactory.getLogger(HttpProcessor.class);

  private HttpClient httpClient;
  private String urlBase;
  private List<String> headers;
  private Metadata metadata;
  private URLBuilder urlBuilder;

  public HttpProcessor(HttpClient httpClient, String urlBase, List<String> headers) {
    this.httpClient = httpClient;
    this.urlBase = urlBase;
    this.headers = Optional.ofNullable(headers).orElse(List.of());
  }

  /**
   * Creates a generic dynamic proxy with a new {@link HttpInvocationHandler
   * HttpInvocationHandler}
   * object which will resolve the requests.
   * 
   * @param <T>            A generic interface.
   * @param interfaceClass Service of a generic interface
   * @return A "virtual" instance for the interface.
   */
  public <T> T createProxy(Class<T> interfaceClass) {
    metadata = MetadataCollector.collect(interfaceClass);
    validateMetadata();
    urlBuilder = new URLBuilder(metadata);
    var httpInvocationHandler = new HttpInvocationHandler(this);
    var proxy = ReflectUtil.createProxy(interfaceClass, httpInvocationHandler);
    logger.debug("Created Instance : {}", interfaceClass.getSimpleName());
    return proxy;
  }

  public Object resolve(Method method, Object[] arguments) {
    var methodName = method.getName();
    var methodMetadata = metadata.getMethods().get(methodName);
    var url = urlBase + urlBuilder.build(methodName, arguments);
    var httpMethod = methodMetadata.getHttpAnnotation().getName();
    var returnType = methodMetadata.getReturnType();
    var isMultipart = methodMetadata.isMultipart();
    var bodyObject = calculateBodyObject(methodMetadata, arguments);
    var fullHeaders = new ArrayList<>(this.headers);
    fullHeaders.addAll(calculateHeaderContentType(bodyObject, isMultipart));
    var fullHeadersArray = fullHeaders.toArray(new String[0]);
    var httpConnector = HttpConnector.builder()
        .httpClient(httpClient)
        .url(url)
        .httpMethod(httpMethod)
        .returnType(returnType)
        .bodyObject(bodyObject)
        .isMultipart(isMultipart)
        .headersArray(fullHeadersArray)
        .build();
    logger.debug("Http Call : {} {}", httpMethod, url);
    return httpConnector.sendRequest();
  }

  private void validateMetadata() {
    metadata.getMethods().forEach((methodName, methodMetadata) -> {
      if (!methodMetadata.isDefault()) {
        Optional.ofNullable(methodMetadata.getHttpAnnotation())
            .orElseThrow(
                () -> new CleverClientException("Missing HTTP anotation for the method {0}.", methodName, null));
      }
    });

    final var PATH = Path.class.getSimpleName();
    metadata.getMethods().forEach((methodName, methodMetadata) -> {
      var url = methodMetadata.getUrl();
      var listPathParams = CommonUtil.findFullMatches(url, Constant.REGEX_PATH_PARAM_URL);
      if (!CommonUtil.isNullOrEmpty(listPathParams)) {
        listPathParams.forEach(pathParam -> methodMetadata.getParametersByType().get(PATH).stream()
            .filter(paramMetadata -> pathParam.equals(paramMetadata.getAnnotationValue())).findFirst()
            .orElseThrow(() -> new CleverClientException(
                "Path param {0} in the url cannot find an annotated argument in the method {1}.", pathParam, methodName,
                null)));
      }
    });
  }

  private Object calculateBodyObject(Metadata.Method methodMetadata, Object[] arguments) {
    final var BODY = Body.class.getSimpleName();
    var indexBody = methodMetadata.getParametersByType().get(BODY).stream()
        .map(Metadata.Parameter::getIndex).findFirst().orElse(-1);
    return indexBody >= 0 ? arguments[indexBody] : null;
  }

  private List<String> calculateHeaderContentType(Object bodyObject, boolean isMultipart) {
    List<String> headerContentType = new ArrayList<>();
    if (bodyObject != null) {
      var contentType = isMultipart
          ? Constant.TYPE_MULTIPART + Constant.BOUNDARY_TITLE + "\"" + Constant.BOUNDARY_VALUE + "\""
          : Constant.TYPE_APP_JSON;
      headerContentType.add(Constant.HEADER_CONTENT_TYPE);
      headerContentType.add(contentType);
    }
    return headerContentType;
  }
}