package io.github.sashirestela.cleverclient.http;

import java.lang.reflect.Method;
import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.github.sashirestela.cleverclient.annotation.BodyPart;
import io.github.sashirestela.cleverclient.annotation.Header;
import io.github.sashirestela.cleverclient.metadata.MethodSignature;
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
    private static final Logger logger = LoggerFactory.getLogger(HttpProcessor.class);

    private final HttpClient httpClient;
    private final String urlBase;
    private final List<String> headers;
    //private Metadata metadata;
    //private URLBuilder urlBuilder;

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
        var metadata = MetadataCollector.collect(interfaceClass);
        validateMetadata(metadata);
        //urlBuilder = new URLBuilder(metadata);
        var httpInvocationHandler = new HttpInvocationHandler(this, metadata);
        var proxy = ReflectUtil.createProxy(interfaceClass, httpInvocationHandler);
        logger.debug("Created Instance : {}", interfaceClass.getSimpleName());
        return proxy;
    }

    public Object resolve(Metadata metadata, Method method, Object[] arguments) {
        var methodName = method.getName();
        var methodSignature = MethodSignature.of(method);
        var methodMetadata = metadata.getMethods().get(methodSignature);
        var url = urlBase + new URLBuilder(metadata).build(methodSignature, arguments);
        var httpMethod = methodMetadata.getHttpAnnotation().getName();
        var returnType = methodMetadata.getReturnType();
        var isMultipart = methodMetadata.isMultipart();
        var bodyObjects = calculateBodyObjects(methodMetadata, arguments);
        var fullHeaders = new ArrayList<>(this.headers);
        fullHeaders.addAll(calculateHeaderContentType(bodyObjects, isMultipart));
        fullHeaders.addAll(calculateExtraHeaders(methodMetadata));
        var fullHeadersArray = fullHeaders.toArray(new String[0]);
        var httpConnector = HttpConnector.builder()
                .httpClient(httpClient)
                .url(url)
                .httpMethod(httpMethod)
                .returnType(returnType)
                .bodyObjects(bodyObjects)
                .isMultipart(isMultipart)
                .headersArray(fullHeadersArray)
                .build();
        logger.debug("Http Call : {} {}", httpMethod, url);
        return httpConnector.sendRequest();
    }

    private void validateMetadata(Metadata metadata) {
        metadata.getMethods().forEach((methodName, methodMetadata) -> {
            if (!methodMetadata.isDefault()) {
                Optional.ofNullable(methodMetadata.getHttpAnnotation())
                        .orElseThrow(
                                () -> new CleverClientException("Missing HTTP anotation for the method {0}.",
                                        methodName, null));
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
                                "Path param {0} in the url cannot find an annotated argument in the method {1}.",
                                pathParam, methodName,
                                null)));
            }
        });
    }

    private List<Object> calculateBodyObjects(Metadata.Method methodMetadata, Object[] arguments) {
        final var BODY      = Body.class.getSimpleName();
        final var BODY_PART = BodyPart.class.getSimpleName();

        return Stream.concat(
                methodMetadata.getParametersByType().get(BODY).stream()
                        .limit(1)
                        .map(param -> arguments[param.getIndex()]),
                methodMetadata.getParametersByType().get(BODY_PART).stream()
                        .map(param -> arguments[param.getIndex()])
                        .filter(arg -> !CommonUtil.isNullOrEmpty(arg))
        ).collect(Collectors.toList());
    }

    private List<String> calculateHeaderContentType(List<Object> bodyObjects, boolean isMultipart) {
        List<String> headerContentType = new ArrayList<>();
        if (!bodyObjects.isEmpty()) {
            var contentType = isMultipart
                    ? Constant.TYPE_MULTIPART + Constant.BOUNDARY_TITLE + "\"" + Constant.BOUNDARY_VALUE + "\""
                    : Constant.TYPE_APP_JSON;
            headerContentType.add(Constant.HEADER_CONTENT_TYPE);
            headerContentType.add(contentType);
        }
        return headerContentType;
    }

    private List<String> calculateExtraHeaders(Metadata.Method methodMetadata) {
        List<String> extraHeaders = new ArrayList<>();
        List<Metadata.Annotation> httpHeaders = methodMetadata.getHttpHeaders();
        for (var httpHeader : httpHeaders) {
            if (httpHeader.getInstance() instanceof Header) {
                Header headerAnnotation = (Header) httpHeader.getInstance();

                extraHeaders.add(headerAnnotation.name());
                extraHeaders.add(headerAnnotation.value());
            }
        }
        return extraHeaders;
    }
}