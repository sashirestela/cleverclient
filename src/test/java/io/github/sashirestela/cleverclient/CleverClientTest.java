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
                .urlBase("https://test")
                .build();
        assertEquals(List.of(), cleverClient.getHeaders());
        assertEquals(HttpClient.Version.HTTP_2, cleverClient.getHttpClient().version());
        assertNotNull(cleverClient.getUrlBase());
        assertNotNull(cleverClient.getHttpProcessor());
    }

    @Test
    void shouldImplementInterfaceWhenCallingCreate() {
        var cleverClient = CleverClient.builder()
                .urlBase("https://test")
                .header("headerName")
                .header("headerValue")
                .httpClient(HttpClient.newHttpClient())
                .endOfStream("[DONE]")
                .build();
        var test = cleverClient.create(TestCleverClient.class);
        assertNotNull(test);
    }

    @Test
    void shouldThrownExceptionWhenTryingToPassAnEmptyUrlBase() {
        var cleverClientBuilder = CleverClient.builder()
                .header("headerName")
                .header("headerValue")
                .httpClient(HttpClient.newHttpClient())
                .endOfStream("[DONE]");
        assertThrows(NullPointerException.class,
                () -> cleverClientBuilder.build());
    }

    @Test
    void shouldThrownExceptionWhenTryingToPassAnOddNumbersOfHeaders() {
        var cleverClientBuilder = CleverClient.builder()
                .urlBase("http://test")
                .header("oneHeader");
        Exception exception = assertThrows(CleverClientException.class,
                () -> cleverClientBuilder.build());
        assertTrue(exception.getMessage().equals("Headers must be entered as pair of values in the list."));
    }

    interface TestCleverClient {
        @GET("/api/")
        CompletableFuture<String> getText();
    }
}
