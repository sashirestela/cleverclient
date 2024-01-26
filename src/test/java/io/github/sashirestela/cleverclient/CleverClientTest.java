package io.github.sashirestela.cleverclient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.http.HttpClient;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.Test;

import io.github.sashirestela.cleverclient.annotation.GET;
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
    void shouldThrownExceptionWhenTryingToPassAnEmptyBaseUrl() {
        var cleverClientBuilder = CleverClient.builder()
                .header("headerName")
                .header("headerValue")
                .httpClient(HttpClient.newHttpClient())
                .endOfStream("[DONE]");
        assertThrows(NullPointerException.class,
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

    interface TestCleverClient {
        @GET("/api/")
        CompletableFuture<String> getText();
    }
}
