package io.github.sashirestela.cleverclient.client;

import io.github.sashirestela.cleverclient.support.CleverClientException;
import io.github.sashirestela.cleverclient.support.ContentType;
import io.github.sashirestela.cleverclient.support.HttpMultipart;
import io.github.sashirestela.cleverclient.support.ReturnType;
import io.github.sashirestela.cleverclient.support.ReturnType.Category;
import io.github.sashirestela.cleverclient.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class JavaHttpClientAdapter extends HttpClientAdapter {

    private static Logger logger = LoggerFactory.getLogger(JavaHttpClientAdapter.class);

    private HttpClient httpClient;
    private EnumMap<Category, FunctionsByCategory> functionsByCategoryMap;

    public JavaHttpClientAdapter(HttpClient httpClient) {
        this.httpClient = httpClient;
        fillFunctionsByCategory();
    }

    public JavaHttpClientAdapter() {
        this(HttpClient.newHttpClient());
    }

    public HttpClient getJavaHttpClient() {
        return httpClient;
    }

    @Override
    protected Object send(RequestData request) {
        var returnType = request.getReturnType();
        var functions = getFunctions(returnType);
        var httpRequest = convertToHttpRequest(request);
        try {
            var httpResponse = httpClient.send(httpRequest, functions.bodyHandler.get());
            logger.debug(RESPONSE_CODE_FORMAT, httpResponse.statusCode());
            var originalResponseData = convertToResponseData(httpResponse);
            throwExceptionIfErrorIsPresent(originalResponseData);
            var responseData = interceptResponse(originalResponseData);
            if (!returnType.isStream()) {
                logger.debug(RESPONSE_FORMAT, responseData.getBody());
                return functions.responseConverter.apply(responseData.getBody(), returnType);
            } else {
                return functions.responseConverter.apply(responseData, returnType);
            }
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CleverClientException(e);
        }
    }

    @Override
    protected Object sendAsync(RequestData request) {
        var returnType = request.getReturnType();
        var functions = getFunctions(returnType);
        var httpRequest = convertToHttpRequest(request);
        var httpResponseFuture = httpClient.sendAsync(httpRequest, functions.bodyHandler.get());
        return httpResponseFuture.thenApply(httpResponse -> {
            logger.debug(RESPONSE_CODE_FORMAT, httpResponse.statusCode());
            var originalResponseData = convertToResponseData(httpResponse);
            throwExceptionIfErrorIsPresent(originalResponseData);
            var responseData = interceptResponse(originalResponseData);
            if (!returnType.isStream()) {
                logger.debug(RESPONSE_FORMAT, responseData.getBody());
                return functions.responseConverter.apply(responseData.getBody(), returnType);
            } else {
                return functions.responseConverter.apply(responseData, returnType);
            }
        });
    }

    @Override
    public void shutdown() {
        httpClient.executor().ifPresent(executor -> {
            if (executor instanceof ExecutorService) {
                ((ExecutorService) executor).shutdown();
            }
        });
    }

    private FunctionsByCategory getFunctions(ReturnType returnType) {
        var functions = functionsByCategoryMap.get(returnType.category());
        if (functions == null) {
            throw new CleverClientException("Unsupported return type {0}.", returnType.getFullClassName(), null);
        }
        return functions;
    }

    private HttpRequest convertToHttpRequest(RequestData request) {
        var bodyPublisher = createBodyPublisher(request.getBody(), request.getContentType());
        var headersArray = request.getHeaders().toArray(new String[0]);
        var httpRequestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(request.getUrl()))
                .method(request.getHttpMethod(), bodyPublisher);
        if (headersArray.length > 0) {
            httpRequestBuilder.headers(headersArray);
        }
        return httpRequestBuilder.build();
    }

    @SuppressWarnings("unchecked")
    private BodyPublisher createBodyPublisher(Object bodyObject, ContentType contentType) {
        BodyPublisher bodyPublisher = null;
        if (contentType == null) {
            logger.debug(REQUEST_BODY_FORMAT, "(Empty)");
            bodyPublisher = BodyPublishers.noBody();
        } else if (contentType == ContentType.MULTIPART_FORMDATA) {
            logger.debug(REQUEST_BODY_FORMAT, bodyObject);
            var bodyBytes = HttpMultipart.toByteArrays((Map<String, Object>) bodyObject);
            bodyPublisher = BodyPublishers.ofByteArrays(bodyBytes);
        } else if (contentType == ContentType.APPLICATION_JSON) {
            logger.debug(REQUEST_BODY_FORMAT, bodyObject);
            bodyPublisher = BodyPublishers.ofString((String) bodyObject);
        }
        return bodyPublisher;
    }

    private ResponseData convertToResponseData(HttpResponse<?> httpResponse) {
        var httpRequest = httpResponse.request();
        return ResponseData.builder()
                .statusCode(httpResponse.statusCode())
                .body(httpResponse.body())
                .headers(httpResponse.headers().map())
                .request(httpRequest != null ? ResponseData.Request.builder()
                        .httpMethod(httpRequest.method())
                        .url(httpRequest.uri().toString())
                        .headers(httpRequest.headers().map())
                        .build() : null)
                .build();
    }

    private class FunctionsByCategory {

        Supplier<BodyHandler<?>> bodyHandler;
        BiFunction<Object, ReturnType, Object> responseConverter;

        public FunctionsByCategory(Supplier<BodyHandler<?>> bodyHandler,
                BiFunction<Object, ReturnType, Object> responseConverter) {
            this.bodyHandler = bodyHandler;
            this.responseConverter = responseConverter;
        }

    }

    private void fillFunctionsByCategory() {
        this.functionsByCategoryMap = new EnumMap<>(Category.class);
        functionsByCategoryMap.put(Category.SYNC_BINARY, new FunctionsByCategory(
                () -> BodyHandlers.ofInputStream(),
                (r, t) -> r));
        functionsByCategoryMap.put(Category.SYNC_PLAIN_TEXT, new FunctionsByCategory(
                () -> BodyHandlers.ofString(),
                (r, t) -> r));
        functionsByCategoryMap.put(Category.SYNC_CUSTOM, new FunctionsByCategory(
                () -> BodyHandlers.ofString(),
                (r, t) -> JsonUtil.jsonToObject((String) r, t.getBaseClass())));
        functionsByCategoryMap.put(Category.SYNC_GENERIC, new FunctionsByCategory(
                () -> BodyHandlers.ofString(),
                (r, t) -> JsonUtil.jsonToParametricObject((String) r, t.getGenericClassIfExists(), t.getBaseClass())));
        functionsByCategoryMap.put(Category.SYNC_LIST, new FunctionsByCategory(
                () -> BodyHandlers.ofString(),
                (r, t) -> JsonUtil.jsonToList((String) r, t.getBaseClass())));
        functionsByCategoryMap.put(Category.SYNC_STREAM, new FunctionsByCategory(
                () -> BodyHandlers.ofLines(),
                (r, t) -> convertToStreamOfObjects((ResponseData) r, t)));
        functionsByCategoryMap.put(Category.SYNC_STREAM_EVENT, new FunctionsByCategory(
                () -> BodyHandlers.ofLines(),
                (r, t) -> convertToStreamOfEvents((ResponseData) r, t)));
        functionsByCategoryMap.put(Category.ASYNC_BINARY, new FunctionsByCategory(
                () -> BodyHandlers.ofInputStream(),
                (r, t) -> r));
        functionsByCategoryMap.put(Category.ASYNC_PLAIN_TEXT, new FunctionsByCategory(
                () -> BodyHandlers.ofString(),
                (r, t) -> r));
        functionsByCategoryMap.put(Category.ASYNC_CUSTOM, new FunctionsByCategory(
                () -> BodyHandlers.ofString(),
                (r, t) -> JsonUtil.jsonToObject((String) r, t.getBaseClass())));
        functionsByCategoryMap.put(Category.ASYNC_GENERIC, new FunctionsByCategory(
                () -> BodyHandlers.ofString(),
                (r, t) -> JsonUtil.jsonToParametricObject((String) r, t.getGenericClassIfExists(), t.getBaseClass())));
        functionsByCategoryMap.put(Category.ASYNC_LIST, new FunctionsByCategory(
                () -> BodyHandlers.ofString(),
                (r, t) -> JsonUtil.jsonToList((String) r, t.getBaseClass())));
        functionsByCategoryMap.put(Category.ASYNC_STREAM, new FunctionsByCategory(
                () -> BodyHandlers.ofLines(),
                (r, t) -> convertToStreamOfObjects((ResponseData) r, t)));
        functionsByCategoryMap.put(Category.ASYNC_STREAM_EVENT, new FunctionsByCategory(
                () -> BodyHandlers.ofLines(),
                (r, t) -> convertToStreamOfEvents((ResponseData) r, t)));
    }

}
