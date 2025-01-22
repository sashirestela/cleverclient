package io.github.sashirestela.cleverclient.websocket;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface WebSocketAdapter {

    CompletableFuture<Void> connect(String url, Map<String, String> headers);

    CompletableFuture<Void> send(String message);

    void close();

    void onMessage(Consumer<String> callback);

    void onOpen(Action callback);

    void onClose(BiConsumer<Integer, String> callback);

    void onError(Consumer<Throwable> callback);

}
