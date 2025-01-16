package io.github.sashirestela.cleverclient.http;

import io.github.sashirestela.cleverclient.client.OkHttpClientAdapter;
import io.github.sashirestela.cleverclient.test.TestSupport.SyncType;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OkHttpProcessorTest implements HttpProcessorTest {

    HttpProcessor httpProcessor;
    OkHttpClient okHttpClient;
    Response okHttpResponse;
    ResponseBody responseBody;
    Request okHttpRequest;
    HttpUrl httpUrl;
    Call call;

    OkHttpProcessorTest() {
        okHttpClient = mock(OkHttpClient.class);
        okHttpResponse = mock(Response.class);
        responseBody = mock(ResponseBody.class);
        call = mock(Call.class);
        okHttpRequest = mock(Request.class);
        httpUrl = mock(HttpUrl.class);
    }

    @Override
    public HttpProcessor getHttpProcessor() {
        httpProcessor = HttpProcessor.builder()
                .baseUrl("https://api.demo")
                .headers(List.of())
                .clientAdapter(new OkHttpClientAdapter(okHttpClient))
                .build();
        return httpProcessor;
    }

    @Override
    public void setMocksForString(SyncType syncType, String result) throws IOException {
        when(okHttpClient.newCall(any(okhttp3.Request.class))).thenReturn(call);

        if (syncType == SyncType.SYNC) {
            when(call.execute()).thenReturn(okHttpResponse);
        } else {
            doAnswer(invocation -> {
                Callback callback = invocation.getArgument(0);
                callback.onResponse(call, okHttpResponse);
                return null;
            }).when(call).enqueue(any(Callback.class));
        }

        when(okHttpResponse.code()).thenReturn(HttpURLConnection.HTTP_OK);
        when(okHttpResponse.headers()).thenReturn(okhttp3.Headers.of(Map.of()));
        when(okHttpResponse.body()).thenReturn(responseBody);
        when(responseBody.string()).thenReturn(result);
    }

    @Override
    public void setMocksForBinary(SyncType syncType, InputStream result) throws IOException {
        when(okHttpClient.newCall(any(okhttp3.Request.class))).thenReturn(call);

        if (syncType == SyncType.SYNC) {
            when(call.execute()).thenReturn(okHttpResponse);
        } else {
            doAnswer(invocation -> {
                Callback callback = invocation.getArgument(0);
                callback.onResponse(call, okHttpResponse);
                return null;
            }).when(call).enqueue(any(Callback.class));
        }

        when(okHttpResponse.code()).thenReturn(HttpURLConnection.HTTP_OK);
        when(okHttpResponse.headers()).thenReturn(okhttp3.Headers.of(Map.of()));
        when(okHttpResponse.body()).thenReturn(responseBody);
        when(responseBody.byteStream()).thenReturn(result);
    }

    @Override
    public void setMocksForStream(SyncType syncType, Stream<String> result) throws IOException {
        when(okHttpClient.newCall(any(okhttp3.Request.class))).thenReturn(call);

        if (syncType == SyncType.SYNC) {
            when(call.execute()).thenReturn(okHttpResponse);
        } else {
            doAnswer(invocation -> {
                Callback callback = invocation.getArgument(0);
                callback.onResponse(call, okHttpResponse);
                return null;
            }).when(call).enqueue(any(Callback.class));
        }

        when(okHttpResponse.code()).thenReturn(HttpURLConnection.HTTP_OK);
        when(okHttpResponse.headers()).thenReturn(okhttp3.Headers.of(Map.of()));
        when(okHttpResponse.body()).thenReturn(responseBody);
        when(responseBody.byteStream()).thenReturn(
                new ByteArrayInputStream(result.collect(Collectors.joining("\n")).getBytes(StandardCharsets.UTF_8)));
    }

    @Override
    public void setMocksForException() throws IOException {
        when(okHttpClient.newCall(any(okhttp3.Request.class))).thenReturn(call);
        when(call.execute()).thenThrow(new IOException("The operation was interrupted"));
    }

    @Override
    public void setMocksForStringWithError(String result) throws IOException {
        when(okHttpClient.newCall(any(okhttp3.Request.class))).thenReturn(call);
        doAnswer(invocation -> {
            Callback callback = invocation.getArgument(0);
            callback.onResponse(call, okHttpResponse);
            return null;
        }).when(call).enqueue(any(Callback.class));
        when(httpUrl.toString()).thenReturn("https://api.com");
        when(okHttpRequest.method()).thenReturn("GET");
        when(okHttpRequest.url()).thenReturn(httpUrl);
        when(okHttpRequest.headers()).thenReturn(okhttp3.Headers.of(Map.of()));
        when(okHttpResponse.code()).thenReturn(HttpURLConnection.HTTP_NOT_FOUND);
        when(okHttpResponse.headers()).thenReturn(okhttp3.Headers.of(Map.of()));
        when(okHttpResponse.body()).thenReturn(responseBody);
        when(responseBody.string()).thenReturn(result);
        when(okHttpResponse.request()).thenReturn(okHttpRequest);
    }

    @Override
    public void setMocksForBinaryWithError(InputStream result) throws IOException {
        when(okHttpClient.newCall(any(okhttp3.Request.class))).thenReturn(call);
        doAnswer(invocation -> {
            Callback callback = invocation.getArgument(0);
            callback.onResponse(call, okHttpResponse);
            return null;
        }).when(call).enqueue(any(Callback.class));
        when(httpUrl.toString()).thenReturn("https://api.com");
        when(okHttpRequest.method()).thenReturn("GET");
        when(okHttpRequest.url()).thenReturn(httpUrl);
        when(okHttpRequest.headers()).thenReturn(okhttp3.Headers.of(Map.of()));
        when(okHttpResponse.code()).thenReturn(HttpURLConnection.HTTP_NOT_FOUND);
        when(okHttpResponse.headers()).thenReturn(okhttp3.Headers.of(Map.of()));
        when(okHttpResponse.body()).thenReturn(responseBody);
        when(responseBody.byteStream()).thenReturn(result);
        when(okHttpResponse.request()).thenReturn(okHttpRequest);
    }

    @Override
    public void setMocksForStreamWithError(Stream<String> result) throws IOException {
        when(okHttpClient.newCall(any(okhttp3.Request.class))).thenReturn(call);
        doAnswer(invocation -> {
            Callback callback = invocation.getArgument(0);
            callback.onResponse(call, okHttpResponse);
            return null;
        }).when(call).enqueue(any(Callback.class));
        when(httpUrl.toString()).thenReturn("https://api.com");
        when(okHttpRequest.method()).thenReturn("GET");
        when(okHttpRequest.url()).thenReturn(httpUrl);
        when(okHttpRequest.headers()).thenReturn(okhttp3.Headers.of(Map.of()));
        when(okHttpResponse.code()).thenReturn(HttpURLConnection.HTTP_NOT_FOUND);
        when(okHttpResponse.headers()).thenReturn(okhttp3.Headers.of(Map.of()));
        when(okHttpResponse.body()).thenReturn(responseBody);
        when(responseBody.byteStream()).thenReturn(
                new ByteArrayInputStream(result.collect(Collectors.joining("\n")).getBytes(StandardCharsets.UTF_8)));
        when(okHttpResponse.request()).thenReturn(okHttpRequest);
    }

    @Override
    public void testShutdown() {
        var defaultAdapter = new OkHttpClientAdapter();
        assertDoesNotThrow(() -> defaultAdapter.shutdown());

        var client = new OkHttpClient();
        var customAdapter = new OkHttpClientAdapter(client);
        assertDoesNotThrow(() -> customAdapter.shutdown());
    }

}
