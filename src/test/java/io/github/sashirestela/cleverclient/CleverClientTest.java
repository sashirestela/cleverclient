package io.github.sashirestela.cleverclient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import io.github.sashirestela.cleverclient.annotation.GET;
import io.github.sashirestela.cleverclient.annotation.Query;
import io.github.sashirestela.cleverclient.annotation.Resource;

class CleverClientTest {

    @Test
    void shouldSetPropertiesToDefaultValuesWhenBuilderIsCalledWithoutThoseProperties() {
        var cleverClient = CleverClient.builder()
                .baseUrl("https://test")
                .build();
        assertEquals(Map.of(), cleverClient.getHeaders());
        assertEquals(HttpClient.Version.HTTP_2, cleverClient.getHttpClient().version());
        assertNotNull(cleverClient.getBaseUrl());
        assertNotNull(cleverClient.getHttpProcessor());
        assertNull(cleverClient.getUrlInterceptor());
    }

    @Test
    void shouldImplementInterfaceWhenCallingCreate() {
        var cleverClient = CleverClient.builder()
                .baseUrl("https://test")
                .header("headerName", "headerValue")
                .httpClient(HttpClient.newHttpClient())
                .endOfStream("[DONE]")
                .build();
        var test = cleverClient.create(TestCleverClient.class);
        assertNotNull(test);
    }

    @Test
    void shouldThrownExceptionWhenTryingToPassAnEmptyBaseUrl() {
        var cleverClientBuilder = CleverClient.builder()
                .header("headerName", "headerValue")
                .httpClient(HttpClient.newHttpClient())
                .endOfStream("[DONE]");
        assertThrows(NullPointerException.class,
                cleverClientBuilder::build);
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldModifyUrlWhenPassingUrlInterceptorFunction() {
        var httpClient = mock(HttpClient.class);
        Function<String, String> customUrlInterceptor = url -> {
            // add a query parameter to url
            url = url + (url.contains("?") ? "&" : "?") + "api-version=2024-01-31";
            // remove '/vN' or '/vN.M' from url
            url = url.replaceFirst("(\\/v\\d+\\.*\\d*)", "");
            return url;
        };
        var cleverClient = CleverClient.builder()
                .baseUrl("https://test")
                .urlInterceptor(customUrlInterceptor)
                .httpClient(httpClient)
                .build();
        when(httpClient.sendAsync(any(), any()))
                .thenReturn(CompletableFuture.completedFuture(mock(HttpResponse.class)));

        var test = cleverClient.create(TestCleverClient.class);
        test.getText("geo");

        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(httpClient, times(1)).sendAsync(requestCaptor.capture(), any());

        var actualUrl = requestCaptor.getValue().uri().toString();
        var expectedUrl = "https://test/api/text?prefix=geo&api-version=2024-01-31";
        assertEquals(expectedUrl, actualUrl);
    }

    @Resource("/v1.2/api")
    interface TestCleverClient {

        @GET("/text")
        CompletableFuture<String> getText(@Query("prefix") String prefix);
    }
}
