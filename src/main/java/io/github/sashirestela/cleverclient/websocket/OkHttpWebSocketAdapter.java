package io.github.sashirestela.cleverclient.websocket;

import io.github.sashirestela.cleverclient.support.CleverClientException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class OkHttpWebSocketAdapter implements WebSocketAdapter {

    private static final Logger logger = LoggerFactory.getLogger(OkHttpWebSocketAdapter.class);
    private OkHttpClient okHttpClient;
    private WebSocket webSocket;
    private Consumer<String> messageCallback;
    private Action openCallback;
    private BiConsumer<Integer, String> closeCallback;
    private Consumer<Throwable> errorCallback;

    public OkHttpWebSocketAdapter(OkHttpClient okHttpClient) {
        this.okHttpClient = okHttpClient;
        logger.debug("Created WebSocketAdapter with custom OkHttpClient");
    }

    public OkHttpWebSocketAdapter() {
        this.okHttpClient = new OkHttpClient();
        logger.debug("Created WebSocketAdapter with default OkHttpClient");
    }

    @Override
    public CompletableFuture<Void> connect(String url, Map<String, String> headers) {
        logger.info("Connecting to WebSocket URL: {}", url);
        logger.debug("Connection headers: {}", headers);

        Request.Builder requestBuilder = new Request.Builder().url(url);
        headers.forEach(requestBuilder::addHeader);

        CompletableFuture<Void> connectFuture = new CompletableFuture<>();
        this.webSocket = okHttpClient.newWebSocket(requestBuilder.build(), new WebSocketListener() {

            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                logger.info("WebSocket connection established with response code: {}", response.code());
                if (openCallback != null) {
                    openCallback.execute();
                }
                connectFuture.complete(null);
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                logger.debug("Received message: {}", text);
                if (messageCallback != null) {
                    messageCallback.accept(text);
                }
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                logger.info("WebSocket closing with code: {}, reason: {}", code, reason);
                if (closeCallback != null) {
                    closeCallback.accept(code, reason);
                }
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                String responseCode = response != null ? String.valueOf(response.code()) : "unknown";
                logger.error("WebSocket error occurred. Response code: {}", responseCode, t);
                if (errorCallback != null) {
                    errorCallback.accept(t);
                }
                connectFuture.completeExceptionally(t);
            }

        });
        return connectFuture;
    }

    @Override
    public CompletableFuture<Void> send(String message) {
        logger.debug("Sending message: {}", message);
        boolean success = webSocket.send(message);
        CompletableFuture<Void> future = new CompletableFuture<>();
        if (success) {
            logger.trace("Message sent successfully");
            future.complete(null);
        } else {
            String errorMsg = "Failed to send message";
            logger.error(errorMsg);
            future.completeExceptionally(new CleverClientException(errorMsg));
        }
        return future;
    }

    @Override
    public void close() {
        if (webSocket != null) {
            logger.info("Initiating WebSocket close");
            webSocket.close(1000, "Closing connection");
            okHttpClient.dispatcher().executorService().shutdown();
            okHttpClient.connectionPool().evictAll();
            logger.debug("WebSocket resources cleaned up");
        }
    }

    @Override
    public void onMessage(Consumer<String> callback) {
        logger.trace("Registering message callback");
        this.messageCallback = callback;
    }

    @Override
    public void onOpen(Action callback) {
        logger.trace("Registering open callback");
        this.openCallback = callback;
    }

    @Override
    public void onClose(BiConsumer<Integer, String> callback) {
        logger.trace("Registering close callback");
        this.closeCallback = callback;
    }

    @Override
    public void onError(Consumer<Throwable> errorCallback) {
        logger.trace("Registering error callback");
        this.errorCallback = errorCallback;
    }

}
