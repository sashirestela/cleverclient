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
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import io.github.sashirestela.cleverclient.annotation.GET;
import io.github.sashirestela.cleverclient.annotation.Query;
import io.github.sashirestela.cleverclient.annotation.Resource;
import io.github.sashirestela.cleverclient.support.CleverClientException;

class CleverClientTest {

    @Test
    void shouldSetPropertiesToDefaultValuesWhenBuilderIsCalledWithoutThoseProperties() {
        var cleverClient = CleverClient.builder()
                .baseUrl("https://test")
                .build();
        assertEquals(List.of(), cleverClient.getHeaders());
        assertEquals(HttpClient.Version.HTTP_2, cleverClient.getHttpClient().version());
        assertNotNull(cleverClient.getBaseUrl());
        assertNotNull(cleverClient.getHttpProcessor());
        assertNull(cleverClient.getUrlInterceptor());
    }

    @Test
    void shouldBuildSuccessfullyUsingDeprecatedUrlBase() {
        var baseUrl = "https://test";
        var cleverClient = CleverClient.builder()
                .urlBase(baseUrl)
                .build();
        assertEquals(List.of(), cleverClient.getHeaders());
        assertEquals(HttpClient.Version.HTTP_2, cleverClient.getHttpClient().version());
        // verify that baseUrl is set when building with the deprecated urlBase() method
        assertEquals(cleverClient.getBaseUrl(), baseUrl);
        assertNotNull(cleverClient.getHttpProcessor());
    }

    @Test
    void shouldImplementInterfaceWhenCallingCreate() {
        var cleverClient = CleverClient.builder()
                .baseUrl("https://test")
                .header("headerName")
                .header("headerValue")
                .httpClient(HttpClient.newHttpClient())
                .endOfStream("[DONE]")
                .build();
        var test = cleverClient.create(TestCleverClient.class);
        assertNotNull(test);
    }

    @Test
    void shouldThrownExceptionWhenTryingToPassAnEmptyBaseUrlAndUrlBase() {
        var cleverClientBuilder = CleverClient.builder()
                .header("headerName")
                .header("headerValue")
                .httpClient(HttpClient.newHttpClient())
                .endOfStream("[DONE]");
        assertThrows(CleverClientException.class,
                cleverClientBuilder::build);
    }

    @Test
    void shouldThrownExceptionWhenTryingToPassAnOddNumbersOfHeaders() {
        var cleverClientBuilder = CleverClient.builder()
                .baseUrl("http://test")
                .header("oneHeader");
        Exception exception = assertThrows(CleverClientException.class,
                cleverClientBuilder::build);
        assertEquals("Headers must be entered as pair of values in the list.",
                exception.getMessage());
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
