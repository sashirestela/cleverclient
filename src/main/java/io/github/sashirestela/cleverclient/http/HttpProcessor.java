package io.github.sashirestela.cleverclient.http;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.sashirestela.cleverclient.metadata.InterfaceMetadata.MethodMetadata;
import io.github.sashirestela.cleverclient.metadata.InterfaceMetadataStore;
import io.github.sashirestela.cleverclient.util.Constant;
import io.github.sashirestela.cleverclient.util.ReflectUtil;

/**
 * HttpProcessor orchestrates all the http interaction.
 */
public class HttpProcessor implements InvocationHandler {
    private static final Logger logger = LoggerFactory.getLogger(HttpProcessor.class);

    private HttpClient httpClient;
    private String urlBase;
    private List<String> headers;

    /**
     * Constructor to create an instance of HttpProcessor.
     * 
     * @param httpClient Java's HttpClient component that solves the http calling.
     * @param urlBase    Root of the url of the API service to call.
     * @param headers    Http headers for all the API service.
     */
    public HttpProcessor(String urlBase, List<String> headers, HttpClient httpClient) {
        this.urlBase = urlBase;
        this.headers = headers;
        this.httpClient = httpClient;
    }

    /**
     * Creates a generic dynamic proxy with this HttpProcessor object acting as an
     * InvocationHandler to resolve the requests arriving to the proxy. Previously,
     * the interface metadata is collected and stored in memory to be used later and
     * avoid to use Reflection calls.
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
     * Method automatically called whenever an interface's method is called. It
     * handles default methods directly. Non-default methods are solved by calling
     * HttpConnector.
     * 
     * @param proxy     The proxy instance that the method was invoked on.
     * @param method    The Method instance corresponding to the interface method
     *                  invoked on the proxy instance.
     * @param arguments An array of objects containing the values of the arguments
     *                  passed in the method invocation on the proxy instance, or
     *                  null if interface method takes no arguments.
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
     * Reads the interface method metadata from memory and uses them to prepare an
     * HttpConnector object that will resend the request to the Java's HttpClient
     * and will receive the response. This method is called from the invoke mehod.
     * 
     * @param method    The Method instance corresponding to the interface method
     *                  invoked on the proxy instance.
     * @param arguments An array of objects containing the values of the arguments
     *                  passed in the method invocation on the proxy instance, or
     *                  null if interface method takes no arguments.
     * @return The response coming from the HttpConnector's sendRequest method.
     */
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
        for (var i = 0; i < headers.size(); i++) {
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