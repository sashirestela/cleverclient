package io.github.sashirestela.cleverclient;

import io.github.sashirestela.cleverclient.annotation.Body;
import io.github.sashirestela.cleverclient.annotation.GET;
import io.github.sashirestela.cleverclient.annotation.Query;
import io.github.sashirestela.cleverclient.annotation.Resource;
import io.github.sashirestela.cleverclient.client.HttpClientAdapter;
import io.github.sashirestela.cleverclient.client.JavaHttpClientAdapter;
import io.github.sashirestela.cleverclient.http.HttpRequestData;
import io.github.sashirestela.cleverclient.retry.RetryConfig;
import io.github.sashirestela.cleverclient.retry.RetryableRequest;
import io.github.sashirestela.cleverclient.support.ContentType;
import io.github.sashirestela.cleverclient.util.HttpRequestBodyTestUtility;
import lombok.Builder;
import lombok.Value;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CleverClientTest {

    @Test
    void shouldSetPropertiesToDefaultValuesWhenBuilderIsCalledWithoutThoseProperties() {
        var cleverClient = CleverClient.builder()
                .baseUrl("https://test")
                .build();
        assertEquals(Map.of(), cleverClient.getHeaders());
        assertNotNull(cleverClient.getBaseUrl());
        assertNotNull(cleverClient.getHttpProcessor());
        assertNotNull(cleverClient.getClientAdapter());
        assertTrue(cleverClient.getClientAdapter() instanceof JavaHttpClientAdapter);
        assertNull(cleverClient.getBodyInspector());
        assertNull(cleverClient.getRequestInterceptor());
        assertNull(cleverClient.getResponseInterceptor());
        assertNull(cleverClient.getRetryConfig());
    }

    @Test
    void shouldSetRetrayableRequestWhenRetryConfigIsProvided() {
        var mockClientAdapter = Mockito.mock(HttpClientAdapter.class);
        var cleverClient = CleverClient.builder()
                .baseUrl("https://api.example.com")
                .clientAdapter(mockClientAdapter)
                .retryConfig(RetryConfig.of())
                .build();
        assertNotNull(cleverClient.getRetryConfig());
        verify(mockClientAdapter).setRetryableRequest(any(RetryableRequest.class));
    }

    @Test
    void shouldImplementInterfaceWhenCallingCreate() {
        var cleverClient = CleverClient.builder()
                .baseUrl("https://test")
                .header("headerName", "headerValue")
                .clientAdapter(new JavaHttpClientAdapter())
                .build();
        var test = cleverClient.create(TestCleverClient.class);
        assertNotNull(test);
    }

    @Test
    void shouldThrownExceptionWhenTryingToPassAnEmptyBaseUrl() {
        var cleverClientBuilder = CleverClient.builder()
                .header("headerName", "headerValue")
                .clientAdapter(new JavaHttpClientAdapter());
        assertThrows(NullPointerException.class,
                cleverClientBuilder::build);
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldModifyRequestWhenPassingInterceptorFunction() {
        var httpClient = mock(HttpClient.class);
        UnaryOperator<HttpRequestData> requestInterceptor = request -> {
            var url = request.getUrl();
            var contentType = request.getContentType();
            var body = request.getBody();

            // add a query parameter to url
            url = url + (url.contains("?") ? "&" : "?") + "api-version=2024-01-31";
            // remove '/vN' or '/vN.M' from url
            url = url.replaceFirst("(\\/v\\d+\\.*\\d*)", "");
            request.setUrl(url);

            if (contentType != null) {
                if (contentType.equals(ContentType.APPLICATION_JSON)) {
                    var bodyJson = (String) request.getBody();
                    // remove a field from body (as Json)
                    bodyJson = bodyJson.replaceFirst(",?\"model\":\"[^\"]*\",?", "");
                    bodyJson = bodyJson.replaceFirst("\"\"", "\",\"");
                    body = bodyJson;
                }
                if (contentType.equals(ContentType.MULTIPART_FORMDATA)) {
                    Map<String, Object> bodyMap = (Map<String, Object>) request.getBody();
                    // remove a field from body (as Map)
                    bodyMap.remove("model");
                    body = bodyMap;
                }
                request.setBody(body);
            }

            return request;
        };
        var cleverClient = CleverClient.builder()
                .baseUrl("https://test")
                .requestInterceptor(requestInterceptor)
                .clientAdapter(new JavaHttpClientAdapter(httpClient))
                .build();
        when(httpClient.sendAsync(any(), any()))
                .thenReturn(CompletableFuture.completedFuture(mock(HttpResponse.class)));

        var test = cleverClient.create(TestCleverClient.class);
        test.getText(Sample.builder().id("1").model("abc").description("sample").build(), "geo");

        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(httpClient, times(1)).sendAsync(requestCaptor.capture(), any());

        var httpRequestCaptor = requestCaptor.getValue();
        var actualUrl = httpRequestCaptor.uri().toString();
        var expectedUrl = "https://test/api/text?prefix=geo&api-version=2024-01-31";
        var actualBody = HttpRequestBodyTestUtility.extractBody(httpRequestCaptor);
        var expectedBody = "{\"id\":\"1\",\"description\":\"sample\"}";
        assertEquals(expectedUrl, actualUrl);
        assertEquals(expectedBody, actualBody);
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldNotThrownExceptionWhenBodyInspectorEndsSuccessfully() {
        var httpClient = mock(HttpClient.class);
        Consumer<Object> bodyInspector = body -> {
            var sample = (Sample) body;
            if (sample.getModel() == null) {
                throw new IllegalArgumentException("The parameter model must not be null.");
            }
        };
        var cleverClient = CleverClient.builder()
                .baseUrl("https://test")
                .bodyInspector(bodyInspector)
                .clientAdapter(new JavaHttpClientAdapter(httpClient))
                .build();
        when(httpClient.sendAsync(any(), any()))
                .thenReturn(CompletableFuture.completedFuture(mock(HttpResponse.class)));
        var testService = cleverClient.create(TestCleverClient.class);
        assertDoesNotThrow(() -> testService.getText(Sample.builder().model("abc").build(), "math"));
    }

    @Test
    void shouldThrownExceptionWhenBodyInspectorFails() {
        Consumer<Object> bodyInspector = body -> {
            var sample = (Sample) body;
            if (sample.getModel() == null) {
                throw new IllegalArgumentException("The parameter model must not be null.");
            }
        };
        var cleverClient = CleverClient.builder()
                .baseUrl("https://test")
                .bodyInspector(bodyInspector)
                .build();
        var testService = cleverClient.create(TestCleverClient.class);
        var sample = Sample.builder().id("1").build();
        assertThrows(IllegalArgumentException.class, () -> testService.getText(sample, "math"));
    }

    @Value
    @Builder
    static class Sample {

        String id;
        String model;
        String description;

    }

    @Resource("/v1.2/api")
    interface TestCleverClient {

        @GET("/text")
        CompletableFuture<String> getText(@Body Sample sample, @Query("prefix") String prefix);

    }

}
