package io.github.sashirestela.cleverclient.websocket;

import io.github.sashirestela.cleverclient.support.CleverClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class JavaHttpWebSocketAdapter extends WebSocketAdapter {

    private static final Logger logger = LoggerFactory.getLogger(JavaHttpWebSocketAdapter.class);
    private HttpClient httpClient;
    private WebSocket webSocket;
    private final StringBuilder dataBuffer = new StringBuilder();
    private CompletableFuture<Void> sendFuture;
    private CompletableFuture<Void> closeFuture;

    public JavaHttpWebSocketAdapter(HttpClient httpClient) {
        this.httpClient = httpClient;
        logger.debug("Created JavaHttpWebSocketAdapter");
    }

    public JavaHttpWebSocketAdapter() {
        this(HttpClient.newHttpClient());
    }

    @Override
    @SuppressWarnings("java:S3776")
    public CompletableFuture<Void> connect(String url, Map<String, String> headers) {
        logger.debug("Connecting to WebSocket URL: {}", url);
        logger.debug("Connection headers: {}", headers);

        WebSocket.Builder builder = this.httpClient.newWebSocketBuilder();
        headers.forEach(builder::header);

        CompletableFuture<Void> connectFuture = new CompletableFuture<>();

        builder.buildAsync(URI.create(url), new WebSocket.Listener() {

            @Override
            public void onOpen(WebSocket webSocket) {
                JavaHttpWebSocketAdapter.this.webSocket = webSocket;
                logger.debug("WebSocket connection established");
                if (openCallback != null) {
                    openCallback.execute();
                }
                connectFuture.complete(null);
                webSocket.request(1);
            }

            @Override
            public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                logger.trace("Received text data chunk, last={}", last);
                dataBuffer.append(data);
                if (last) {
                    if (messageCallback != null) {
                        String completeMessage = dataBuffer.toString();
                        logger.debug("Received message: {}", completeMessage);
                        messageCallback.accept(completeMessage);
                    }
                    dataBuffer.setLength(0);
                    if (sendFuture != null) {
                        sendFuture.complete(null);
                        sendFuture = null;
                    }
                }
                webSocket.request(1);
                return CompletableFuture.completedFuture(null);
            }

            @Override
            public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
                logger.debug("WebSocket closing with code: {}, reason: {}", statusCode, reason);
                if (closeCallback != null) {
                    closeCallback.accept(statusCode, reason);
                }
                if (closeFuture != null) {
                    closeFuture.complete(null);
                }
                return CompletableFuture.completedFuture(null);
            }

            @Override
            public void onError(WebSocket webSocket, Throwable error) {
                logger.error("WebSocket error occurred", error);
                if (errorCallback != null) {
                    errorCallback.accept(error);
                }
                if (sendFuture != null) {
                    sendFuture.completeExceptionally(error);
                }
                if (closeFuture != null) {
                    closeFuture.completeExceptionally(error);
                }
                connectFuture.completeExceptionally(error);
            }

        });

        return connectFuture;
    }

    @Override
    public CompletableFuture<Void> send(String message) {
        if (webSocket == null) {
            logger.error("Attempt to send message before WebSocket connection is established");
            CompletableFuture<Void> future = new CompletableFuture<>();
            future.completeExceptionally(new CleverClientException("WebSocket is not connected"));
            return future;
        }

        logger.debug("Sending message: {}", message);
        sendFuture = new CompletableFuture<>();
        webSocket.sendText(message, true);
        return sendFuture;
    }

    @Override
    public void close() {
        if (webSocket != null) {
            logger.debug("Initiating WebSocket close");
            closeFuture = new CompletableFuture<>();
            webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "Closing connection");
            try {
                closeFuture.join();
                logger.debug("WebSocket close completed normally");
            } catch (Exception e) {
                logger.error("Error during WebSocket close", e);
                if (errorCallback != null) {
                    errorCallback.accept(e);
                }
            }
        }
    }

}
