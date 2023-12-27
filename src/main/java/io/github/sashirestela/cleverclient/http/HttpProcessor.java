package io.github.sashirestela.cleverclient.http;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.sashirestela.cleverclient.metadata.InterfaceMetadata.MethodMetadata;
import io.github.sashirestela.cleverclient.metadata.InterfaceMetadataStore;
import io.github.sashirestela.cleverclient.util.Constant;
import io.github.sashirestela.cleverclient.util.ReflectUtil;

public class HttpProcessor implements InvocationHandler {
    private static final Logger logger = LoggerFactory.getLogger(HttpProcessor.class);

    private HttpClient httpClient;
    private String urlBase;
    private List<String> headers;

    public HttpProcessor(HttpClient httpClient, String urlBase, List<String> headers) {
        this.httpClient = httpClient;
        this.urlBase = urlBase;
        this.headers = Optional.ofNullable(headers).orElse(List.of());
    }

    /**
     * Creates a generic dynamic proxy with a new {@link HttpInvocationHandler
     * HttpInvocationHandler} object which will resolve the requests.
     * 
     * @param <T>            A generic interface.
     * @param interfaceClass Service of a generic interface
     * @return A "virtual" instance for the interface.
     */
    public <T> T createProxy(Class<T> interfaceClass) {
        InterfaceMetadataStore.one().save(interfaceClass);
        var proxy = ReflectUtil.createProxy(interfaceClass, this);
        logger.debug("Created Instance : {}", interfaceClass.getSimpleName());
        return proxy;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] arguments) throws Throwable {
        logger.debug("Invoked Method : {}.{}()", method.getDeclaringClass().getSimpleName(), method.getName());
        if (method.isDefault()) {
            return MethodHandles.lookup()
                    .findSpecial(
                            method.getDeclaringClass(),
                            method.getName(),
                            MethodType.methodType(
                                    method.getReturnType(),
                                    method.getParameterTypes()),
                            method.getDeclaringClass())
                    .bindTo(proxy)
                    .invokeWithArguments(arguments);
        } else {
            var responseObject = resolve(method, arguments);
            logger.debug("Received Response");
            return responseObject;
        }
    }

    private Object resolve(Method method, Object[] arguments) {
        var interfaceMetadata = InterfaceMetadataStore.one().get(method.getDeclaringClass());
        var methodMetadata = interfaceMetadata.getMethodBySignature().get(method.toString());
        var urlMethod = interfaceMetadata.getFullUrlByMethod(methodMetadata);
        var url = urlBase + URLBuilder.one().build(urlMethod, methodMetadata, arguments);
        var httpMethod = methodMetadata.getHttpAnnotationName();
        var returnType = methodMetadata.getReturnType();
        var isMultipart = methodMetadata.isMultipart();
        var bodyObject = calculateBodyObject(methodMetadata, arguments);
        var fullHeaders = new ArrayList<>(this.headers);
        fullHeaders.addAll(calculateHeaderContentType(bodyObject, isMultipart));
        fullHeaders.addAll(interfaceMetadata.getFullHeadersByMethod(methodMetadata));
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
        logger.debug("Request Headers : {}", printHeaders(fullHeaders));
        return httpConnector.sendRequest();
    }

    private Object calculateBodyObject(MethodMetadata methodMetadata, Object[] arguments) {
        var indexBody = methodMetadata.getBodyIndex();
        return indexBody >= 0 ? arguments[indexBody] : null;
    }

    private List<String> calculateHeaderContentType(Object bodyObject, boolean isMultipart) {
        List<String> headerContentType = new ArrayList<>();
        if (bodyObject != null) {
            headerContentType.add(Constant.HEADER_CONTENT_TYPE);
            var contentType = isMultipart
                    ? Constant.TYPE_MULTIPART + Constant.BOUNDARY_TITLE + "\"" + Constant.BOUNDARY_VALUE + "\""
                    : Constant.TYPE_APP_JSON;
            headerContentType.add(contentType);
        }
        return headerContentType;
    }

    private String printHeaders(List<String> headers) {
        var print = "{";
        for(var i = 0; i < headers.size(); i++) {
            if (i > 1) {
                print += ", ";
            }
            var headerKey = headers.get(i++);
            var headerVal = headerKey.equals("Authorization") ? "*".repeat(10) : headers.get(i);
            print += headerKey + " = " + headerVal;
        }
        print += "}";
        return print;
    }
}