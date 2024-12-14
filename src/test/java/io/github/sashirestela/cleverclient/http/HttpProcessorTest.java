package io.github.sashirestela.cleverclient.http;

import io.github.sashirestela.cleverclient.support.CleverClientException;
import io.github.sashirestela.cleverclient.support.Configurator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
@Order(2)
class HttpProcessorTest {

    HttpProcessor httpProcessor;
    HttpClient httpClient = mock(HttpClient.class);
    HttpResponse<String> httpResponse = mock(HttpResponse.class);
    HttpResponse<Stream<String>> httpResponseStream = mock(HttpResponse.class);
    HttpResponse<InputStream> httpResponseBinary = mock(HttpResponse.class);
    HttpRequest httpRequest = mock(HttpRequest.class);
    HttpHeaders httpHeaders = mock(HttpHeaders.class);

    @BeforeAll
    static void setup() {
        Configurator.builder()
                .endOfStream("END")
                .build();
    }

    @BeforeEach
    void init() {
        httpProcessor = HttpProcessor.builder()
                .baseUrl("https://api.demo")
                .headers(List.of())
                .httpClient(httpClient)
                .build();
    }

    @Test
    void shouldThownExceptionWhenCallingMethodReturnTypeIsUnsupported() {
        var service = httpProcessor.createProxy(ITest.AsyncService.class);
        Exception exception = assertThrows(CleverClientException.class,
                () -> service.unsupportedMethod());
        assertTrue(exception.getMessage().contains("Unsupported return type"));
    }

    @Test
    void shouldReturnAStringSyncWhenMethodReturnTypeIsAString() throws IOException, InterruptedException {
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandlers.ofString().getClass())))
                .thenReturn(httpResponse);
        when(httpResponse.statusCode()).thenReturn(HttpURLConnection.HTTP_OK);
        when(httpResponse.body()).thenReturn("{\"id\":100,\"description\":\"Description\",\"active\":true}");

        var service = httpProcessor.createProxy(ITest.SyncService.class);
        var actualDemo = service.getDemoPlain(100);
        var expectedDemo = "{\"id\":100,\"description\":\"Description\",\"active\":true}";

        assertEquals(expectedDemo, actualDemo);
    }

    @Test
    void shouldThrownExceptionWhenMethodReturnTypeIsAString() throws IOException, InterruptedException {
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandlers.ofString().getClass())))
                .thenThrow(new InterruptedException("The operation was interrupted"));

        var service = httpProcessor.createProxy(ITest.SyncService.class);
        assertThrows(CleverClientException.class, () -> service.getDemoPlain(100));
    }

    @Test
    void shouldshouldReturnABinarySyncWhenMethodReturnTypeIsABinary() throws IOException, InterruptedException {
        InputStream binaryData = new FileInputStream(new File("src/test/resources/image.png"));
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandlers.ofInputStream().getClass())))
                .thenReturn(httpResponseBinary);
        when(httpResponseBinary.statusCode()).thenReturn(HttpURLConnection.HTTP_OK);
        when(httpResponseBinary.body()).thenReturn(binaryData);

        var service = httpProcessor.createProxy(ITest.SyncService.class);
        var actualDemo = service.getDemoBinary(100);
        var expectedDemo = binaryData;

        assertEquals(expectedDemo, actualDemo);
    }

    @Test
    void shouldThrownExceptionWhenMethodReturnTypeIsABinary() throws IOException, InterruptedException {
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandlers.ofInputStream().getClass())))
                .thenThrow(new InterruptedException("The operation was interrupted"));

        var service = httpProcessor.createProxy(ITest.SyncService.class);
        assertThrows(CleverClientException.class, () -> service.getDemoBinary(100));
    }

    @Test
    void shouldReturnAnObjectSyncWhenMethodReturnTypeIsAnObject() throws IOException, InterruptedException {
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandlers.ofString().getClass())))
                .thenReturn(httpResponse);
        when(httpResponse.statusCode()).thenReturn(HttpURLConnection.HTTP_OK);
        when(httpResponse.body()).thenReturn("{\"id\":100,\"description\":\"Description\",\"active\":true}");

        var service = httpProcessor.createProxy(ITest.SyncService.class);
        var actualDemo = service.getDemo(100);
        var expectedDemo = new ITest.Demo(100, "Description", true);

        assertEquals(expectedDemo, actualDemo);
    }

    @Test
    void shouldThrownExceptionWhenMethodReturnTypeIsAnObject() throws IOException, InterruptedException {
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandlers.ofString().getClass())))
                .thenThrow(new InterruptedException("The operation was interrupted"));

        var service = httpProcessor.createProxy(ITest.SyncService.class);
        assertThrows(CleverClientException.class, () -> service.getDemo(100));
    }

    @Test
    void shouldReturnAGenericSyncWhenMethodReturnTypeIsAnObject() throws IOException, InterruptedException {
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandlers.ofString().getClass())))
                .thenReturn(httpResponse);
        when(httpResponse.statusCode()).thenReturn(HttpURLConnection.HTTP_OK);
        when(httpResponse.body())
                .thenReturn("{\"id\":1,\"listDemo\":[{\"id\":100,\"description\":\"Description\",\"active\":true}]}");

        var service = httpProcessor.createProxy(ITest.SyncService.class);
        var actualGenericDemo = service.getGenericDemo(1);
        var actualDemo = actualGenericDemo.getListDemo().get(0);
        var expectedDemo = new ITest.Demo(100, "Description", true);

        assertEquals(expectedDemo, actualDemo);
    }

    @Test
    void shouldThrownExceptionWhenMethodReturnTypeIsAGenericObject() throws IOException, InterruptedException {
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandlers.ofString().getClass())))
                .thenThrow(new InterruptedException("The operation was interrupted"));

        var service = httpProcessor.createProxy(ITest.SyncService.class);
        assertThrows(CleverClientException.class, () -> service.getGenericDemo(1));
    }

    @Test
    void shouldReturnAListSyncWhenMethodReturnTypeIsAList() throws IOException, InterruptedException {
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandlers.ofString().getClass())))
                .thenReturn(httpResponse);
        when(httpResponse.statusCode()).thenReturn(HttpURLConnection.HTTP_OK);
        when(httpResponse.body()).thenReturn("[{\"id\":100,\"description\":\"Description\",\"active\":true}]");

        var service = httpProcessor.createProxy(ITest.SyncService.class);
        var actualListDemo = service.getDemos();
        var actualDemo = actualListDemo.get(0);
        var expectedDemo = new ITest.Demo(100, "Description", true);

        assertEquals(expectedDemo, actualDemo);
    }

    @Test
    void shouldThrownExceptionWhenMethodReturnTypeIsAList() throws IOException, InterruptedException {
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandlers.ofString().getClass())))
                .thenThrow(new InterruptedException("The operation was interrupted"));

        var service = httpProcessor.createProxy(ITest.SyncService.class);
        assertThrows(CleverClientException.class, () -> service.getDemos());
    }

    @Test
    void shouldReturnAStreamSyncWhenMethodReturnTypeIsAStream() throws IOException, InterruptedException {
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandlers.ofLines().getClass())))
                .thenReturn(httpResponseStream);
        when(httpResponseStream.statusCode()).thenReturn(HttpURLConnection.HTTP_OK);
        when(httpResponseStream.body())
                .thenReturn(Stream.of("data: {\"id\":100,\"description\":\"Description\",\"active\":true}"));

        var service = httpProcessor.createProxy(ITest.SyncService.class);
        var actualStreamDemo = service.getDemoStream(new ITest.RequestDemo("Descr", null));
        var actualDemo = actualStreamDemo.findFirst().get();
        var expectedDemo = new ITest.Demo(100, "Description", true);

        assertEquals(expectedDemo, actualDemo);
    }

    @Test
    void shouldThrownExceptionWhenMethodReturnTypeIsAStream() throws IOException, InterruptedException {
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandlers.ofLines().getClass())))
                .thenThrow(new InterruptedException("The operation was interrupted"));

        var service = httpProcessor.createProxy(ITest.SyncService.class);
        var requestDemo = new ITest.RequestDemo("Descr", null);
        assertThrows(CleverClientException.class, () -> service.getDemoStream(requestDemo));
    }

    @Test
    void shouldReturnAStreamSyncWhenMethodReturnTypeIsAStreamEvent() throws IOException, InterruptedException {
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandlers.ofLines().getClass())))
                .thenReturn(httpResponseStream);
        when(httpResponseStream.statusCode()).thenReturn(HttpURLConnection.HTTP_OK);
        when(httpResponseStream.body())
                .thenReturn(Stream.of("event: created",
                        "data: {\"id\":100,\"description\":\"Description\",\"active\":true}"));

        var service = httpProcessor.createProxy(ITest.SyncService.class);
        var actualStreamObject = service.getStreamEvent(new ITest.RequestDemo("Descr", null));
        var actualObject = actualStreamObject.findFirst().get().getData();
        var expectedObject = new ITest.Demo(100, "Description", true);

        assertEquals(expectedObject, actualObject);
    }

    @Test
    void shouldThrownExceptionWhenMethodReturnTypeIsAStreamObject() throws IOException, InterruptedException {
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandlers.ofLines().getClass())))
                .thenThrow(new InterruptedException("The operation was interrupted"));

        var service = httpProcessor.createProxy(ITest.SyncService.class);
        var requestDemo = new ITest.RequestDemo("Descr", null);
        assertThrows(CleverClientException.class, () -> service.getStreamEvent(requestDemo));
    }

    @Test
    void shouldReturnAStringAsyncWhenMethodReturnTypeIsAString() {
        when(httpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandlers.ofString().getClass())))
                .thenReturn(CompletableFuture.completedFuture(httpResponse));
        when(httpResponse.statusCode()).thenReturn(HttpURLConnection.HTTP_OK);
        when(httpResponse.body()).thenReturn("{\"id\":100,\"description\":\"Description\",\"active\":true}");

        var service = httpProcessor.createProxy(ITest.AsyncService.class);
        var actualDemo = service.getDemoPlain(100).join();
        var expectedDemo = "{\"id\":100,\"description\":\"Description\",\"active\":true}";

        assertEquals(expectedDemo, actualDemo);
    }

    @Test
    void shouldReturnABinaryAsyncWhenMethodReturnTypeIsABinary() throws FileNotFoundException {
        InputStream binaryData = new FileInputStream(new File("src/test/resources/image.png"));
        when(httpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandlers.ofInputStream().getClass())))
                .thenReturn(CompletableFuture.completedFuture(httpResponseBinary));
        when(httpResponseBinary.statusCode()).thenReturn(HttpURLConnection.HTTP_OK);
        when(httpResponseBinary.body()).thenReturn(binaryData);

        var service = httpProcessor.createProxy(ITest.AsyncService.class);
        var actualDemo = service.getDemoBinary(100).join();
        var expectedDemo = binaryData;

        assertEquals(expectedDemo, actualDemo);
    }

    @Test
    void shouldReturnAnObjectAsyncWhenMethodReturnTypeIsAnObject() {
        when(httpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandlers.ofString().getClass())))
                .thenReturn(CompletableFuture.completedFuture(httpResponse));
        when(httpResponse.statusCode()).thenReturn(HttpURLConnection.HTTP_OK);
        when(httpResponse.body()).thenReturn("{\"id\":100,\"description\":\"Description\",\"active\":true}");

        var service = httpProcessor.createProxy(ITest.AsyncService.class);
        var actualDemo = service.getDemo(100).join();
        var expectedDemo = new ITest.Demo(100, "Description", true);

        assertEquals(expectedDemo, actualDemo);
    }

    @Test
    void shouldReturnAGenericAsyncWhenMethodReturnTypeIsAnObject() {
        when(httpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandlers.ofString().getClass())))
                .thenReturn(CompletableFuture.completedFuture(httpResponse));
        when(httpResponse.statusCode()).thenReturn(HttpURLConnection.HTTP_OK);
        when(httpResponse.body())
                .thenReturn("{\"id\":1,\"listDemo\":[{\"id\":100,\"description\":\"Description\",\"active\":true}]}");

        var service = httpProcessor.createProxy(ITest.AsyncService.class);
        var actualGenericDemo = service.getGenericDemo(1).join();
        var actualDemo = actualGenericDemo.getListDemo().get(0);
        var expectedDemo = new ITest.Demo(100, "Description", true);

        assertEquals(expectedDemo, actualDemo);
    }

    @Test
    void shouldReturnAListAsyncWhenMethodReturnTypeIsAList() {
        when(httpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandlers.ofString().getClass())))
                .thenReturn(CompletableFuture.completedFuture(httpResponse));
        when(httpResponse.statusCode()).thenReturn(HttpURLConnection.HTTP_OK);
        when(httpResponse.body()).thenReturn("[{\"id\":100,\"description\":\"Description\",\"active\":true}]");

        var service = httpProcessor.createProxy(ITest.AsyncService.class);
        var actualListDemo = service.getDemos().join();
        var actualDemo = actualListDemo.get(0);
        var expectedDemo = new ITest.Demo(100, "Description", true);

        assertEquals(expectedDemo, actualDemo);
    }

    @Test
    void shouldReturnAStreamAsyncWhenMethodReturnTypeIsAStream() {
        when(httpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandlers.ofLines().getClass())))
                .thenReturn(CompletableFuture.completedFuture(httpResponseStream));
        when(httpResponseStream.statusCode()).thenReturn(HttpURLConnection.HTTP_OK);
        when(httpResponseStream.body())
                .thenReturn(Stream.of("data: {\"id\":100,\"description\":\"Description\",\"active\":true}"));

        var service = httpProcessor.createProxy(ITest.AsyncService.class);
        var actualStreamDemo = service.getDemoStream(new ITest.RequestDemo("Descr", null)).join();
        var actualDemo = actualStreamDemo.findFirst().get();
        var expectedDemo = new ITest.Demo(100, "Description", true);

        assertEquals(expectedDemo, actualDemo);
    }

    @Test
    void shouldReturnAStreamAsyncWhenMethodReturnTypeIsAStreamEvent() {
        when(httpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandlers.ofLines().getClass())))
                .thenReturn(CompletableFuture.completedFuture(httpResponseStream));
        when(httpResponseStream.statusCode()).thenReturn(HttpURLConnection.HTTP_OK);
        when(httpResponseStream.body())
                .thenReturn(Stream.of("event: created",
                        "data: {\"id\":100,\"description\":\"Description\",\"active\":true}"));

        var service = httpProcessor.createProxy(ITest.AsyncService.class);
        var actualStreamObject = service.getStreamEvent(new ITest.RequestDemo("Descr", null)).join();
        var actualObject = actualStreamObject.findFirst().get().getData();
        var expectedObject = new ITest.Demo(100, "Description", true);

        assertEquals(expectedObject, actualObject);
    }

    @Test
    void shouldReturnAnObjectWhenMethodIsAnnotatedWithMultipart() {
        when(httpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandlers.ofString().getClass())))
                .thenReturn(CompletableFuture.completedFuture(httpResponse));
        when(httpResponse.statusCode()).thenReturn(HttpURLConnection.HTTP_OK);
        when(httpResponse.body()).thenReturn("{\"id\":100,\"description\":\"Description\",\"active\":true}");

        var service = httpProcessor.createProxy(ITest.AsyncService.class);
        var actualDemo = service.getFile(new ITest.RequestDemo("Descr", Paths.get("src/test/resources/image.png")))
                .join();
        var expectedDemo = new ITest.Demo(100, "Description", true);

        assertEquals(expectedDemo, actualDemo);
    }

    @Test
    void shouldThrownExceptionWhenCallingNoStreamingMethodAndServerRespondsWithError() throws URISyntaxException {
        when(httpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandlers.ofString().getClass())))
                .thenReturn(CompletableFuture.completedFuture(httpResponse));
        when(httpHeaders.map()).thenReturn(Map.of());
        when(httpRequest.method()).thenReturn("GET");
        when(httpRequest.uri()).thenReturn(new URI("https://api.com"));
        when(httpRequest.headers()).thenReturn(httpHeaders);
        when(httpResponse.statusCode()).thenReturn(HttpURLConnection.HTTP_NOT_FOUND);
        when(httpResponse.body()).thenReturn(
                "{\"error\": {\"message\": \"The resource does not exist\", \"type\": \"T\", \"param\": \"P\", \"code\": \"C\"}}");
        when(httpResponse.headers()).thenReturn(httpHeaders);
        when(httpResponse.request()).thenReturn(httpRequest);

        var service = httpProcessor.createProxy(ITest.AsyncService.class);
        var futureService = service.getDemo(100);

        Exception exception = assertThrows(CompletionException.class, () -> futureService.join());
        CleverClientException nestedException = (CleverClientException) exception.getCause();
        assertNotNull(nestedException);
        assertEquals(CleverClientException.class, nestedException.getClass());
        assertTrue(nestedException.responseInfo().get().getData().contains("The resource does not exist"));
    }

    @Test
    void shouldThrownExceptionWhenCallingStreamingMethodAndServerRespondsWithError() throws URISyntaxException {
        when(httpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandlers.ofLines().getClass())))
                .thenReturn(CompletableFuture.completedFuture(httpResponseStream));
        when(httpHeaders.map()).thenReturn(Map.of());
        when(httpRequest.method()).thenReturn("GET");
        when(httpRequest.uri()).thenReturn(new URI("https://api.com"));
        when(httpRequest.headers()).thenReturn(httpHeaders);
        when(httpResponseStream.statusCode()).thenReturn(HttpURLConnection.HTTP_NOT_FOUND);
        when(httpResponseStream.body()).thenReturn(Stream.of(
                "{\"error\": {\"message\": \"The resource does not exist\", \"type\": \"T\", \"param\": \"P\", \"code\": \"C\"}}"));
        when(httpResponseStream.headers()).thenReturn(httpHeaders);
        when(httpResponseStream.request()).thenReturn(httpRequest);

        var service = httpProcessor.createProxy(ITest.AsyncService.class);
        var futureService = service.getDemoStream(new ITest.RequestDemo("Descr", null));

        Exception exception = assertThrows(CompletionException.class, () -> futureService.join());
        CleverClientException nestedException = (CleverClientException) exception.getCause();
        assertNotNull(nestedException);
        assertEquals(CleverClientException.class, nestedException.getClass());
        assertTrue(nestedException.responseInfo().get().getData().contains("The resource does not exist"));
    }

    @Test
    void shouldThrownExceptionWhenCallingBinaryMethodAndServerRespondsWithError() throws URISyntaxException {
        when(httpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandlers.ofInputStream().getClass())))
                .thenReturn(CompletableFuture.completedFuture(httpResponseBinary));
        when(httpHeaders.map()).thenReturn(Map.of());
        when(httpRequest.method()).thenReturn("GET");
        when(httpRequest.uri()).thenReturn(new URI("https://api.com"));
        when(httpRequest.headers()).thenReturn(httpHeaders);
        when(httpResponseBinary.statusCode()).thenReturn(HttpURLConnection.HTTP_NOT_FOUND);
        when(httpResponseBinary.body()).thenReturn(new ByteArrayInputStream(
                "{\"error\": {\"message\": \"The resource does not exist\", \"type\": \"T\", \"param\": \"P\", \"code\": \"C\"}}"
                        .getBytes()));
        when(httpResponseBinary.headers()).thenReturn(httpHeaders);
        when(httpResponseBinary.request()).thenReturn(httpRequest);

        var service = httpProcessor.createProxy(ITest.AsyncService.class);
        var futureService = service.getDemoBinary(100);

        Exception exception = assertThrows(CompletionException.class, () -> futureService.join());
        CleverClientException nestedException = (CleverClientException) exception.getCause();
        assertNotNull(nestedException);
        assertEquals(CleverClientException.class, nestedException.getClass());
        assertTrue(nestedException.responseInfo().get().getData().contains("The resource does not exist"));
    }

    @Test
    void shouldExecuteDefaultMethodWhenItIsCalled() {
        var service = httpProcessor.createProxy(ITest.AsyncService.class);
        var actualValue = service.defaultMethod("Test");
        var expectedValue = "Hello Test";

        assertEquals(expectedValue, actualValue);
    }

}
