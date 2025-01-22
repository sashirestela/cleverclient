package io.github.sashirestela.cleverclient.websocket;

import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class OkHttpWebSocketAdapterTest {

    @Mock
    private OkHttpClient mockOkHttpClient;

    @Mock
    private WebSocket mockWebSocket;

    @Mock
    private Response mockResponse;

    @Captor
    private ArgumentCaptor<WebSocketListener> listenerCaptor;

    private OkHttpWebSocketAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new OkHttpWebSocketAdapter(mockOkHttpClient);
    }

    @Test
    void testConnectSuccess() {
        Action mockOpenAction = mock(Action.class);
        adapter.onOpen(mockOpenAction);

        CompletableFuture<Void> connectFuture = adapter.connect("ws://test", Map.of());

        verify(mockOkHttpClient).newWebSocket(any(Request.class), listenerCaptor.capture());
        WebSocketListener listener = listenerCaptor.getValue();
        when(mockResponse.code()).thenReturn(101);
        listener.onOpen(mockWebSocket, mockResponse);

        assertTrue(connectFuture.isDone());
        assertDoesNotThrow(() -> connectFuture.get());
        verify(mockOpenAction).execute();
    }

    @Test
    void testOnMessage() {
        Consumer<String> mockMessageCallback = mock(Consumer.class);
        adapter.onMessage(mockMessageCallback);
        adapter.connect("ws://test", Map.of());

        verify(mockOkHttpClient).newWebSocket(any(Request.class), listenerCaptor.capture());
        WebSocketListener listener = listenerCaptor.getValue();
        listener.onMessage(mockWebSocket, "test message");

        verify(mockMessageCallback).accept("test message");
    }

    @Test
    void testSendSuccess() {
        when(mockOkHttpClient.newWebSocket(any(Request.class), any(WebSocketListener.class)))
                .thenReturn(mockWebSocket);
        adapter.connect("ws://test", Map.of());
        verify(mockOkHttpClient).newWebSocket(any(Request.class), listenerCaptor.capture());
        WebSocketListener listener = listenerCaptor.getValue();
        listener.onOpen(mockWebSocket, mockResponse);

        when(mockWebSocket.send("test")).thenReturn(true);
        CompletableFuture<Void> sendFuture = adapter.send("test");

        assertTrue(sendFuture.isDone());
        assertDoesNotThrow(() -> sendFuture.get());
        verify(mockWebSocket).send("test");
    }

    @Test
    void testSendFailure() {
        when(mockOkHttpClient.newWebSocket(any(Request.class), any(WebSocketListener.class)))
                .thenReturn(mockWebSocket);
        adapter.connect("ws://test", Map.of());
        verify(mockOkHttpClient).newWebSocket(any(Request.class), listenerCaptor.capture());
        WebSocketListener listener = listenerCaptor.getValue();
        listener.onOpen(mockWebSocket, mockResponse);

        when(mockWebSocket.send("test")).thenReturn(false);
        CompletableFuture<Void> sendFuture = adapter.send("test");

        assertTrue(sendFuture.isCompletedExceptionally());
    }

    @Test
    void testCloseCallback() {
        BiConsumer<Integer, String> mockCloseCallback = mock(BiConsumer.class);
        adapter.onClose(mockCloseCallback);
        adapter.connect("ws://test", Map.of());

        verify(mockOkHttpClient).newWebSocket(any(Request.class), listenerCaptor.capture());
        WebSocketListener listener = listenerCaptor.getValue();
        listener.onClosing(mockWebSocket, 1000, "Normal closure");

        verify(mockCloseCallback).accept(1000, "Normal closure");
    }

    @Test
    void testErrorCallback() {
        Consumer<Throwable> mockErrorCallback = mock(Consumer.class);
        adapter.onError(mockErrorCallback);
        adapter.connect("ws://test", Map.of());

        verify(mockOkHttpClient).newWebSocket(any(Request.class), listenerCaptor.capture());
        WebSocketListener listener = listenerCaptor.getValue();
        Throwable testError = new RuntimeException("Test error");
        listener.onFailure(mockWebSocket, testError, mockResponse);

        verify(mockErrorCallback).accept(testError);
    }

    @Test
    void testClose() {
        Dispatcher mockDispatcher = mock(Dispatcher.class);
        ExecutorService mockExecutorService = mock(ExecutorService.class);
        ConnectionPool mockConnectionPool = mock(ConnectionPool.class);

        when(mockOkHttpClient.newWebSocket(any(Request.class), any(WebSocketListener.class)))
                .thenReturn(mockWebSocket);

        when(mockOkHttpClient.dispatcher()).thenReturn(mockDispatcher);
        when(mockDispatcher.executorService()).thenReturn(mockExecutorService);
        when(mockOkHttpClient.connectionPool()).thenReturn(mockConnectionPool);

        adapter.connect("ws://test", Map.of());
        verify(mockOkHttpClient).newWebSocket(any(Request.class), listenerCaptor.capture());
        WebSocketListener listener = listenerCaptor.getValue();

        listener.onOpen(mockWebSocket, mockResponse);

        adapter.close();

        verify(mockWebSocket).close(1000, "Closing connection");
        verify(mockExecutorService).shutdown();
        verify(mockConnectionPool).evictAll();
    }

}
