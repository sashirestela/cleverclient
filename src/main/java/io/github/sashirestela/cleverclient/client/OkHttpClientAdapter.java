package io.github.sashirestela.cleverclient.client;

import io.github.sashirestela.cleverclient.support.CleverClientException;
import io.github.sashirestela.cleverclient.support.ContentType;
import io.github.sashirestela.cleverclient.support.HttpMultipart;
import io.github.sashirestela.cleverclient.support.ReturnType;
import io.github.sashirestela.cleverclient.support.ReturnType.Category;
import io.github.sashirestela.cleverclient.util.JsonUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.stream.Stream;

public class OkHttpClientAdapter extends HttpClientAdapter {

    private static Logger logger = LoggerFactory.getLogger(OkHttpClientAdapter.class);

    private OkHttpClient okHttpClient;
    private EnumMap<Category, FunctionsByCategory> functionsByCategoryMap;

    public OkHttpClientAdapter(OkHttpClient okHttpClient) {
        this.okHttpClient = okHttpClient;
        fillFunctionsByCategory();
    }

    public OkHttpClientAdapter() {
        this(new OkHttpClient());
    }

    public OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }

    @Override
    protected Object send(RequestData request) {
        var returnType = request.getReturnType();
        var functions = getFunctions(returnType);
        var okHttpRequest = convertToOkHttpRequest(request);
        try {
            var response = okHttpClient.newCall(okHttpRequest).execute();
            logger.debug(RESPONSE_CODE_FORMAT, response.code());
            var responseContent = getReponseContent(response.body(), returnType);
            throwExceptionIfErrorIsPresent(convertToResponseData(response, responseContent));
            if (!returnType.isStream()) {
                logger.debug(RESPONSE_FORMAT, responseContent);
            }
            return functions.responseConverter.apply(responseContent, returnType);
        } catch (IOException e) {
            throw new CleverClientException(e);
        }
    }

    @Override
    protected Object sendAsync(RequestData request) {
        var returnType = request.getReturnType();
        var functions = getFunctions(returnType);
        var okHttpRequest = convertToOkHttpRequest(request);
        var responseFuture = new CompletableFuture<>();
        okHttpClient.newCall(okHttpRequest).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                responseFuture.completeExceptionally(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                logger.debug(RESPONSE_CODE_FORMAT, response.code());
                var responseContent = getReponseContent(response.body(), returnType);
                try {
                    throwExceptionIfErrorIsPresent(convertToResponseData(response, responseContent));
                    if (!returnType.isStream()) {
                        logger.debug(RESPONSE_FORMAT, responseContent);
                    }
                    responseFuture.complete(functions.responseConverter.apply(responseContent, returnType));
                } catch (CleverClientException e) {
                    responseFuture.completeExceptionally(e);
                }
            }

        });
        return responseFuture;
    }

    @Override
    public void shutdown() {
        okHttpClient.dispatcher().executorService().shutdown();
        okHttpClient.connectionPool().evictAll();
    }

    private FunctionsByCategory getFunctions(ReturnType returnType) {
        var functions = functionsByCategoryMap.get(returnType.category());
        if (functions == null) {
            throw new CleverClientException("Unsupported return type {0}.", returnType.getFullClassName(), null);
        }
        return functions;
    }

    private Request convertToOkHttpRequest(RequestData request) {
        var requestBody = createRequestBody(request.getBody(), request.getContentType());
        var headersArray = request.getHeaders().toArray(new String[0]);
        var requestBuilder = new Request.Builder()
                .url(request.getUrl())
                .method(request.getHttpMethod(), requestBody);
        if (headersArray.length > 0) {
            requestBuilder.headers(Headers.of(headersArray));
        }
        return requestBuilder.build();
    }

    @SuppressWarnings("unchecked")
    private RequestBody createRequestBody(Object bodyObject, ContentType contentType) {
        RequestBody requestBody = null;
        if (contentType == null) {
            logger.debug(REQUEST_BODY_FORMAT, "(Empty)");
        } else if (contentType == ContentType.MULTIPART_FORMDATA) {
            logger.debug(REQUEST_BODY_FORMAT, bodyObject);
            var bodyBytes = HttpMultipart.toByteArrays((Map<String, Object>) bodyObject);
            requestBody = RequestBody.create(ByteString.of(concatenateByteArrays(bodyBytes)),
                    MediaType.parse(contentType.getMimeType() + contentType.getDetails()));
        } else if (contentType == ContentType.APPLICATION_JSON) {
            logger.debug(REQUEST_BODY_FORMAT, bodyObject);
            requestBody = RequestBody.create((String) bodyObject, MediaType.parse(contentType.getMimeType()));
        }
        return requestBody;
    }

    private byte[] concatenateByteArrays(List<byte[]> arrays) {
        int totalLength = arrays.stream().mapToInt(arr -> arr.length).sum();
        byte[] result = new byte[totalLength];
        int currentIndex = 0;
        for (byte[] array : arrays) {
            System.arraycopy(array, 0, result, currentIndex, array.length);
            currentIndex += array.length;
        }
        return result;
    }

    private ResponseData convertToResponseData(Response response, Object responseContent) {
        var request = response.request();
        return ResponseData.builder()
                .statusCode(response.code())
                .body(responseContent)
                .headers(response.headers().toMultimap())
                .request(request != null ? ResponseData.Request.builder()
                        .httpMethod(request.method())
                        .url(request.url().toString())
                        .headers(request.headers().toMultimap())
                        .build() : null)
                .build();
    }

    private Object getReponseContent(ResponseBody responseBody, ReturnType returnType) {
        try {
            if (returnType.isStream()) {
                var reader = new BufferedReader(new InputStreamReader(responseBody.byteStream()));
                return reader.lines();
            } else if (returnType.isInputStream()) {
                return responseBody.byteStream();
            } else {
                return responseBody.string();
            }
        } catch (IOException e) {
            throw new CleverClientException(e);
        }
    }

    private class FunctionsByCategory {

        BiFunction<Object, ReturnType, Object> responseConverter;

        public FunctionsByCategory(BiFunction<Object, ReturnType, Object> responseConverter) {
            this.responseConverter = responseConverter;
        }

    }

    @SuppressWarnings("unchecked")
    private void fillFunctionsByCategory() {
        this.functionsByCategoryMap = new EnumMap<>(Category.class);
        functionsByCategoryMap.put(Category.SYNC_BINARY, new FunctionsByCategory(
                (r, t) -> r));
        functionsByCategoryMap.put(Category.SYNC_PLAIN_TEXT, new FunctionsByCategory(
                (r, t) -> r));
        functionsByCategoryMap.put(Category.SYNC_CUSTOM, new FunctionsByCategory(
                (r, t) -> JsonUtil.jsonToObject((String) r, t.getBaseClass())));
        functionsByCategoryMap.put(Category.SYNC_GENERIC, new FunctionsByCategory(
                (r, t) -> JsonUtil.jsonToParametricObject((String) r, t.getGenericClassIfExists(), t.getBaseClass())));
        functionsByCategoryMap.put(Category.SYNC_LIST, new FunctionsByCategory(
                (r, t) -> JsonUtil.jsonToList((String) r, t.getBaseClass())));
        functionsByCategoryMap.put(Category.SYNC_STREAM, new FunctionsByCategory(
                (r, t) -> convertToStreamOfObjects((Stream<String>) r, t)));
        functionsByCategoryMap.put(Category.SYNC_STREAM_EVENT, new FunctionsByCategory(
                (r, t) -> convertToStreamOfEvents((Stream<String>) r, t)));
        functionsByCategoryMap.put(Category.ASYNC_BINARY, new FunctionsByCategory(
                (r, t) -> r));
        functionsByCategoryMap.put(Category.ASYNC_PLAIN_TEXT, new FunctionsByCategory(
                (r, t) -> r));
        functionsByCategoryMap.put(Category.ASYNC_CUSTOM, new FunctionsByCategory(
                (r, t) -> JsonUtil.jsonToObject((String) r, t.getBaseClass())));
        functionsByCategoryMap.put(Category.ASYNC_GENERIC, new FunctionsByCategory(
                (r, t) -> JsonUtil.jsonToParametricObject((String) r, t.getGenericClassIfExists(), t.getBaseClass())));
        functionsByCategoryMap.put(Category.ASYNC_LIST, new FunctionsByCategory(
                (r, t) -> JsonUtil.jsonToList((String) r, t.getBaseClass())));
        functionsByCategoryMap.put(Category.ASYNC_STREAM, new FunctionsByCategory(
                (r, t) -> convertToStreamOfObjects((Stream<String>) r, t)));
        functionsByCategoryMap.put(Category.ASYNC_STREAM_EVENT, new FunctionsByCategory(
                (r, t) -> convertToStreamOfEvents((Stream<String>) r, t)));

    }

}
