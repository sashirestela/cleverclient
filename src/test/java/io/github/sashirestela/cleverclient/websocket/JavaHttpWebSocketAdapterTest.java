package io.github.sashirestela.cleverclient.websocket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class JavaHttpWebSocketAdapterTest {

    @Mock
    private HttpClient mockHttpClient;

    @Mock
    private WebSocket.Builder mockWebSocketBuilder;

    @Mock
    private WebSocket mockWebSocket;

    @Captor
    private ArgumentCaptor<WebSocket.Listener> listenerCaptor;

    private JavaHttpWebSocketAdapter adapter;

    @BeforeEach
    void setUp() {
        lenient().when(mockHttpClient.newWebSocketBuilder()).thenReturn(mockWebSocketBuilder);
        lenient().when(mockWebSocketBuilder.buildAsync(any(URI.class), any(WebSocket.Listener.class)))
                .thenReturn(CompletableFuture.completedFuture(mockWebSocket));

        adapter = new JavaHttpWebSocketAdapter(mockHttpClient);
    }

    @Test
    void testConnectSuccess() {
        Action mockOpenAction = mock(Action.class);
        adapter.onOpen(mockOpenAction);

        CompletableFuture<Void> connectFuture = adapter.connect("ws://test", Map.of("key", "value"));

        verify(mockWebSocketBuilder).buildAsync(any(URI.class), listenerCaptor.capture());
        WebSocket.Listener listener = listenerCaptor.getValue();
        listener.onOpen(mockWebSocket);

        assertTrue(connectFuture.isDone());
        assertDoesNotThrow(() -> connectFuture.get());
        verify(mockOpenAction).execute();
    }

    @Test
    void testOnMessage() {
        Consumer<String> mockMessageCallback = mock(Consumer.class);
        adapter.onMessage(mockMessageCallback);

        adapter.connect("ws://test", Map.of());

        verify(mockWebSocketBuilder).buildAsync(any(URI.class), listenerCaptor.capture());
        WebSocket.Listener listener = listenerCaptor.getValue();

        listener.onText(mockWebSocket, "Hello ", false);
        listener.onText(mockWebSocket, "World", true);

        verify(mockMessageCallback).accept("Hello World");
    }

    @Test
    void testSendSuccess() {
        adapter.connect("ws://test", Map.of());

        verify(mockWebSocketBuilder).buildAsync(any(URI.class), listenerCaptor.capture());
        WebSocket.Listener listener = listenerCaptor.getValue();

        listener.onOpen(mockWebSocket);

        CompletableFuture<Void> sendFuture = adapter.send("test");

        verify(mockWebSocket).sendText("test", true);

        listener.onText(mockWebSocket, "response", true);

        assertTrue(sendFuture.isDone());
        assertDoesNotThrow(() -> sendFuture.get());
    }

    @Test
    void testSendBeforeConnection() {
        CompletableFuture<Void> sendFuture = adapter.send("test");

        assertTrue(sendFuture.isCompletedExceptionally());
        assertThrows(Exception.class, sendFuture::get, "Expected exception not thrown");
        verify(mockWebSocket, never()).sendText(anyString(), anyBoolean());
    }

    @Test
    void testCloseCallback() {
        BiConsumer<Integer, String> mockCloseCallback = mock(BiConsumer.class);
        adapter.onClose(mockCloseCallback);

        adapter.connect("ws://test", Map.of());

        verify(mockWebSocketBuilder).buildAsync(any(URI.class), listenerCaptor.capture());
        WebSocket.Listener listener = listenerCaptor.getValue();

        listener.onClose(mockWebSocket, 1000, "Normal closure");

        verify(mockCloseCallback).accept(1000, "Normal closure");
    }

    @Test
    void testErrorCallback() {
        Consumer<Throwable> mockErrorCallback = mock(Consumer.class);
        adapter.onError(mockErrorCallback);

        adapter.connect("ws://test", Map.of());

        verify(mockWebSocketBuilder).buildAsync(any(URI.class), listenerCaptor.capture());
        WebSocket.Listener listener = listenerCaptor.getValue();

        Throwable testError = new RuntimeException("Test error");
        listener.onError(mockWebSocket, testError);

        verify(mockErrorCallback).accept(testError);
    }

    @Test
    void testClose() {
        adapter.connect("ws://test", Map.of());
        verify(mockWebSocketBuilder).buildAsync(any(URI.class), listenerCaptor.capture());
        WebSocket.Listener listener = listenerCaptor.getValue();

        listener.onOpen(mockWebSocket);

        doAnswer(invocation -> {
            int statusCode = invocation.getArgument(0);
            String reason = invocation.getArgument(1);
            listener.onClose(mockWebSocket, statusCode, reason);
            return null;
        }).when(mockWebSocket).sendClose(anyInt(), anyString());

        assertDoesNotThrow(() -> adapter.close());
        verify(mockWebSocket).sendClose(WebSocket.NORMAL_CLOSURE, "Closing connection");
    }

}
