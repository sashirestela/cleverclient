package io.github.sashirestela.cleverclient.client;

import io.github.sashirestela.cleverclient.Event;
import io.github.sashirestela.cleverclient.support.CleverClientException;
import io.github.sashirestela.cleverclient.support.CleverClientSSE;
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
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Stream;

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

    @Override
    protected Object send(RequestData request) {
        var returnType = request.getReturnType();
        var functions = getFunctions(returnType);
        var httpRequest = convertToHttpRequest(request);
        try {
            var httpResponse = httpClient.send(httpRequest, functions.bodyHandler.get());
            logger.debug("Response Code : {}", httpResponse.statusCode());
            throwExceptionIfErrorIsPresent(convertToResponseData(httpResponse));
            var response = httpResponse.body();
            if (!returnType.isStream()) {
                logger.debug("Response : {}", response);
            }
            return functions.responseConverter.apply(response, returnType);
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
            logger.debug("Response Code : {}", httpResponse.statusCode());
            throwExceptionIfErrorIsPresent(convertToResponseData(httpResponse));
            var response = httpResponse.body();
            if (!returnType.isStream()) {
                logger.debug("Response : {}", response);
            }
            return functions.responseConverter.apply(response, returnType);
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
            logger.debug("Request Body : (Empty)");
            bodyPublisher = BodyPublishers.noBody();
        } else if (contentType == ContentType.MULTIPART_FORMDATA) {
            logger.debug("Request Body : {}", bodyObject);
            var bodyBytes = HttpMultipart.toByteArrays((Map<String, Object>) bodyObject);
            bodyPublisher = BodyPublishers.ofByteArrays(bodyBytes);
        } else if (contentType == ContentType.APPLICATION_JSON) {
            logger.debug("Request Body : {}", bodyObject);
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

    @SuppressWarnings("unchecked")
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
                (r, t) -> convertToStreamOfObjects((Stream<String>) r, t)));
        functionsByCategoryMap.put(Category.SYNC_STREAM_EVENT, new FunctionsByCategory(
                () -> BodyHandlers.ofLines(),
                (r, t) -> convertToStreamOfEvents((Stream<String>) r, t)));
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
                (r, t) -> convertToStreamOfObjects((Stream<String>) r, t)));
        functionsByCategoryMap.put(Category.ASYNC_STREAM_EVENT, new FunctionsByCategory(
                () -> BodyHandlers.ofLines(),
                (r, t) -> convertToStreamOfEvents((Stream<String>) r, t)));
    }

    private Stream<?> convertToStreamOfObjects(Stream<String> response, ReturnType returnType) {
        final var lineRecord = new CleverClientSSE.LineRecord();
        return response
                .map(line -> {
                    logger.debug("Response : {}", line);
                    lineRecord.updateWith(line);
                    return new CleverClientSSE(lineRecord);
                })
                .filter(CleverClientSSE::isActualData)
                .map(item -> JsonUtil.jsonToObject(item.getActualData(), returnType.getBaseClass()));
    }

    private Stream<?> convertToStreamOfEvents(Stream<String> response, ReturnType returnType) {
        final var lineRecord = new CleverClientSSE.LineRecord();
        final var events = returnType.getClassByEvent().keySet();

        return response
                .map(line -> {
                    logger.debug("Response : {}", line);
                    lineRecord.updateWith(line);
                    return new CleverClientSSE(lineRecord, events);
                })
                .filter(CleverClientSSE::isActualData)
                .map(item -> Event.builder()
                        .name(item.getMatchedEvent())
                        .data(JsonUtil.jsonToObject(item.getActualData(),
                                returnType.getClassByEvent().get(item.getMatchedEvent())))
                        .build());
    }

}
