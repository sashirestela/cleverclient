package io.github.sashirestela.cleverclient.http;

import io.github.sashirestela.cleverclient.client.JavaHttpClientAdapter;
import io.github.sashirestela.cleverclient.test.TestSupport.SyncType;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
class JavaHttpProcessorTest implements HttpProcessorTest {

    HttpProcessor httpProcessor;
    HttpClient httpClient = mock(HttpClient.class);
    HttpResponse<String> httpResponse = mock(HttpResponse.class);
    HttpResponse<Stream<String>> httpResponseStream = mock(HttpResponse.class);
    HttpResponse<InputStream> httpResponseBinary = mock(HttpResponse.class);
    HttpRequest httpRequest = mock(HttpRequest.class);
    HttpHeaders httpHeaders = mock(HttpHeaders.class);

    JavaHttpProcessorTest() {
        httpClient = mock(HttpClient.class);
        httpResponse = mock(HttpResponse.class);
        httpResponseStream = mock(HttpResponse.class);
        httpResponseBinary = mock(HttpResponse.class);
        httpRequest = mock(HttpRequest.class);
        httpHeaders = mock(HttpHeaders.class);
    }

    @Override
    public HttpProcessor getHttpProcessor() {
        httpProcessor = HttpProcessor.builder()
                .baseUrl("https://api.demo")
                .headers(List.of())
                .clientAdapter(new JavaHttpClientAdapter(httpClient))
                .build();
        return httpProcessor;
    }

    @Override
    public void setMocksForString(SyncType syncType, String result) throws IOException, InterruptedException {
        if (syncType == SyncType.SYNC) {
            when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandlers.ofString().getClass())))
                    .thenReturn(httpResponse);
        } else {
            when(httpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandlers.ofString().getClass())))
                    .thenReturn(CompletableFuture.completedFuture(httpResponse));
        }
        when(httpResponse.statusCode()).thenReturn(HttpURLConnection.HTTP_OK);
        when(httpResponse.headers()).thenReturn(HttpHeaders.of(Map.of(), (t, s) -> true));
        when(httpResponse.body()).thenReturn(result);
    }

    @Override
    public void setMocksForBinary(SyncType syncType, InputStream result) throws IOException, InterruptedException {
        if (syncType == SyncType.SYNC) {
            when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandlers.ofInputStream().getClass())))
                    .thenReturn(httpResponseBinary);
        } else {
            when(httpClient.sendAsync(any(HttpRequest.class),
                    any(HttpResponse.BodyHandlers.ofInputStream().getClass())))
                    .thenReturn(CompletableFuture.completedFuture(httpResponseBinary));
        }
        when(httpResponseBinary.statusCode()).thenReturn(HttpURLConnection.HTTP_OK);
        when(httpResponseBinary.headers()).thenReturn(HttpHeaders.of(Map.of(), (t, s) -> true));
        when(httpResponseBinary.body()).thenReturn(result);
    }

    @Override
    public void setMocksForStream(SyncType syncType, Stream<String> result) throws IOException, InterruptedException {
        if (syncType == SyncType.SYNC) {
            when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandlers.ofLines().getClass())))
                    .thenReturn(httpResponseStream);
        } else {
            when(httpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandlers.ofLines().getClass())))
                    .thenReturn(CompletableFuture.completedFuture(httpResponseStream));
        }
        when(httpResponseStream.statusCode()).thenReturn(HttpURLConnection.HTTP_OK);
        when(httpResponseStream.headers()).thenReturn(HttpHeaders.of(Map.of(), (t, s) -> true));
        when(httpResponseStream.body()).thenReturn(result);
    }

    @Override
    public void setMocksForException() throws IOException, InterruptedException {
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandlers.ofString().getClass())))
                .thenThrow(new InterruptedException("The operation was interrupted"));
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandlers.ofInputStream().getClass())))
                .thenThrow(new InterruptedException("The operation was interrupted"));
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandlers.ofLines().getClass())))
                .thenThrow(new InterruptedException("The operation was interrupted"));
    }

    @Override
    public void setMocksForStringWithError(String result) throws IOException, URISyntaxException {
        when(httpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandlers.ofString().getClass())))
                .thenReturn(CompletableFuture.completedFuture(httpResponse));
        when(httpHeaders.map()).thenReturn(Map.of());
        when(httpRequest.method()).thenReturn("GET");
        when(httpRequest.uri()).thenReturn(new URI("https://api.com"));
        when(httpRequest.headers()).thenReturn(httpHeaders);
        when(httpResponse.statusCode()).thenReturn(HttpURLConnection.HTTP_NOT_FOUND);
        when(httpResponse.body()).thenReturn(result);
        when(httpResponse.headers()).thenReturn(httpHeaders);
        when(httpResponse.request()).thenReturn(httpRequest);
    }

    @Override
    public void setMocksForBinaryWithError(InputStream result) throws IOException, URISyntaxException {
        when(httpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandlers.ofInputStream().getClass())))
                .thenReturn(CompletableFuture.completedFuture(httpResponseBinary));
        when(httpHeaders.map()).thenReturn(Map.of());
        when(httpRequest.method()).thenReturn("GET");
        when(httpRequest.uri()).thenReturn(new URI("https://api.com"));
        when(httpRequest.headers()).thenReturn(httpHeaders);
        when(httpResponseBinary.statusCode()).thenReturn(HttpURLConnection.HTTP_NOT_FOUND);
        when(httpResponseBinary.body()).thenReturn(result);
        when(httpResponseBinary.headers()).thenReturn(httpHeaders);
        when(httpResponseBinary.request()).thenReturn(httpRequest);
    }

    @Override
    public void setMocksForStreamWithError(Stream<String> result) throws IOException, URISyntaxException {
        when(httpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandlers.ofLines().getClass())))
                .thenReturn(CompletableFuture.completedFuture(httpResponseStream));
        when(httpHeaders.map()).thenReturn(Map.of());
        when(httpRequest.method()).thenReturn("GET");
        when(httpRequest.uri()).thenReturn(new URI("https://api.com"));
        when(httpRequest.headers()).thenReturn(httpHeaders);
        when(httpResponseStream.statusCode()).thenReturn(HttpURLConnection.HTTP_NOT_FOUND);
        when(httpResponseStream.body()).thenReturn(result);
        when(httpResponseStream.headers()).thenReturn(httpHeaders);
        when(httpResponseStream.request()).thenReturn(httpRequest);
    }

}
