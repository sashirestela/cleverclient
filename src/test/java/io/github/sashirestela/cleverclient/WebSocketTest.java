package io.github.sashirestela.cleverclient;

import io.github.sashirestela.cleverclient.websocket.Action;
import io.github.sashirestela.cleverclient.websocket.JavaHttpWebSocketAdapter;
import io.github.sashirestela.cleverclient.websocket.WebSocketAdapter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WebSocketTest {

    private static final String BASE_URL = "ws://example.com/socket";

    @Mock
    private WebSocketAdapter mockWebSocketAdapter;

    private CleverClient.WebSocket webSocket;

    @Test
    void shouldSetPropertiesToDefaultValuesWhenBuilderIsCalledWithoutThoseProperties() {
        webSocket = CleverClient.WebSocket.builder()
                .baseUrl(BASE_URL)
                .build();
        assertEquals(Map.of(), webSocket.getQueryParams());
        assertEquals(Map.of(), webSocket.getHeaders());
        assertNotNull(webSocket.getWebSockewAdapter());
        assertTrue(webSocket.getWebSockewAdapter() instanceof JavaHttpWebSocketAdapter);
    }

    @Test
    void testConnect() {
        CompletableFuture<Void> expectedFuture = CompletableFuture.completedFuture(null);
        when(mockWebSocketAdapter.connect(anyString(), anyMap())).thenReturn(expectedFuture);

        webSocket = CleverClient.WebSocket.builder()
                .baseUrl(BASE_URL)
                .webSockewAdapter(mockWebSocketAdapter)
                .build();

        CompletableFuture<Void> resultFuture = webSocket.connect();

        assertEquals(expectedFuture, resultFuture);
        verify(mockWebSocketAdapter).connect(BASE_URL, Map.of());
    }

    @Test
    void testSend() {
        String message = "test message";
        CompletableFuture<Void> expectedFuture = CompletableFuture.completedFuture(null);
        when(mockWebSocketAdapter.send(message)).thenReturn(expectedFuture);

        webSocket = CleverClient.WebSocket.builder()
                .baseUrl(BASE_URL)
                .webSockewAdapter(mockWebSocketAdapter)
                .build();

        CompletableFuture<Void> resultFuture = webSocket.send(message);

        assertEquals(expectedFuture, resultFuture);
        verify(mockWebSocketAdapter).send(message);
    }

    @Test
    void testClose() {
        webSocket = CleverClient.WebSocket.builder()
                .baseUrl(BASE_URL)
                .webSockewAdapter(mockWebSocketAdapter)
                .build();

        webSocket.close();

        verify(mockWebSocketAdapter).close();
    }

    @Test
    void testOnMessage() {
        Consumer<String> callback = message -> {
        };

        webSocket = CleverClient.WebSocket.builder()
                .baseUrl(BASE_URL)
                .webSockewAdapter(mockWebSocketAdapter)
                .build();

        webSocket.onMessage(callback);

        verify(mockWebSocketAdapter).onMessage(callback);
    }

    @Test
    void testOnOpen() {
        Action callback = () -> {
        };

        webSocket = CleverClient.WebSocket.builder()
                .baseUrl(BASE_URL)
                .webSockewAdapter(mockWebSocketAdapter)
                .build();

        webSocket.onOpen(callback);

        verify(mockWebSocketAdapter).onOpen(callback);
    }

    @Test
    void testOnClose() {
        BiConsumer<Integer, String> callback = (code, reason) -> {
        };

        webSocket = CleverClient.WebSocket.builder()
                .baseUrl(BASE_URL)
                .webSockewAdapter(mockWebSocketAdapter)
                .build();

        webSocket.onClose(callback);

        verify(mockWebSocketAdapter).onClose(callback);
    }

    @Test
    void testOnError() {
        Consumer<Throwable> callback = throwable -> {
        };

        webSocket = CleverClient.WebSocket.builder()
                .baseUrl(BASE_URL)
                .webSockewAdapter(mockWebSocketAdapter)
                .build();

        webSocket.onError(callback);

        verify(mockWebSocketAdapter).onError(callback);
    }

}
