package io.github.sashirestela.cleverclient.http;

import io.github.sashirestela.cleverclient.client.HttpClientAdapter;
import io.github.sashirestela.cleverclient.client.RequestData;
import io.github.sashirestela.cleverclient.metadata.InterfaceMetadata.MethodMetadata;
import io.github.sashirestela.cleverclient.metadata.InterfaceMetadataStore;
import io.github.sashirestela.cleverclient.support.ContentType;
import io.github.sashirestela.cleverclient.util.JsonUtil;
import io.github.sashirestela.cleverclient.util.ReflectUtil;
import lombok.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * HttpProcessor orchestrates all the http interaction.
 */
@Builder
public class HttpProcessor implements InvocationHandler {

    private static final Logger logger = LoggerFactory.getLogger(HttpProcessor.class);

    private final String baseUrl;
    private final List<String> headers;
    private final HttpClientAdapter clientAdapter;
    private final Consumer<Object> bodyInspector;

    /**
     * Creates a generic dynamic proxy with this HttpProcessor object acting as an InvocationHandler to
     * resolve the requests arriving to the proxy. Previously, the interface metadata is collected and
     * stored in memory to be used later and avoid to use Reflection calls.
     * 
     * @param <T>            Type of the interface.
     * @param interfaceClass The interface to be instanced.
     * @return A proxy instance of the interface.
     */
    public <T> T createProxy(Class<T> interfaceClass) {
        InterfaceMetadataStore.one().save(interfaceClass);
        var proxy = ReflectUtil.createProxy(interfaceClass, this);
        logger.debug("Created Instance : {}", interfaceClass.getSimpleName());
        return proxy;
    }

    /**
     * Method automatically called whenever an interface's method is called. It handles default methods
     * directly. Non-default methods are solved by calling HttpConnector.
     * 
     * @param proxy     The proxy instance that the method was invoked on.
     * @param method    The Method instance corresponding to the interface method invoked on the proxy
     *                  instance.
     * @param arguments An array of objects containing the values of the arguments passed in the method
     *                  invocation on the proxy instance, or null if interface method takes no
     *                  arguments.
     * @return The value to return from the method invocation on the proxy instance.
     */
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

    /**
     * Reads the interface method metadata from memory and uses them to prepare an HttpConnector object
     * that will resend the request to the Java's HttpClient and will receive the response. This method
     * is called from the invoke method.
     * 
     * @param method    The Method instance corresponding to the interface method invoked on the proxy
     *                  instance.
     * @param arguments An array of objects containing the values of the arguments passed in the method
     *                  invocation on the proxy instance, or null if interface method takes no
     *                  arguments.
     * @return The response coming from the HttpConnector's sendRequest method.
     */
    private Object resolve(Method method, Object[] arguments) {
        var interfaceMetadata = InterfaceMetadataStore.one().get(method.getDeclaringClass());
        var methodMetadata = interfaceMetadata.getMethodBySignature().get(method.toString());
        var urlMethod = interfaceMetadata.getFullUrlByMethod(methodMetadata);
        var url = baseUrl + URLBuilder.one().build(urlMethod, methodMetadata, arguments);
        var httpMethod = methodMetadata.getHttpAnnotationName();
        var returnType = methodMetadata.getReturnType();
        var contentType = methodMetadata.getContentType();
        var body = getAndInspectBody(methodMetadata, arguments);
        var bodyObject = getBodyObject(body, contentType);
        var fullHeaders = new ArrayList<>(this.headers);
        fullHeaders.addAll(calculateHeaderContentType(contentType));
        fullHeaders.addAll(interfaceMetadata.getFullHeadersByMethod(methodMetadata));
        var request = RequestData.builder()
                .url(url)
                .httpMethod(httpMethod)
                .returnType(returnType)
                .body(bodyObject)
                .contentType(contentType)
                .headers(fullHeaders)
                .build();
        return clientAdapter.sendRequest(request);
    }

    private Object getAndInspectBody(MethodMetadata methodMetadata, Object[] arguments) {
        var bodyIndex = methodMetadata.getBodyIndex();
        var body = bodyIndex >= 0 ? arguments[bodyIndex] : null;

        if (body != null && bodyInspector != null) {
            bodyInspector.accept(body);
        }

        return body;
    }

    private Object getBodyObject(Object body, ContentType contentType) {
        Object bodyObject = null;
        if (body != null) {
            if (contentType == ContentType.MULTIPART_FORMDATA) {
                bodyObject = JsonUtil.objectToMap(body);
            } else if (contentType == ContentType.APPLICATION_JSON) {
                bodyObject = JsonUtil.objectToJson(body);
            }
        }
        return bodyObject;
    }

    private List<String> calculateHeaderContentType(ContentType contentType) {
        final String HEADER_CONTENT_TYPE = "Content-Type";
        List<String> headerContentType = new ArrayList<>();
        if (contentType != null) {
            headerContentType.add(HEADER_CONTENT_TYPE);
            headerContentType.add(contentType.getMimeType() + contentType.getDetails());
        }
        return headerContentType;
    }

}
