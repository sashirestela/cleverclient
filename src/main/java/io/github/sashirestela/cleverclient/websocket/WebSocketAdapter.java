package io.github.sashirestela.cleverclient.websocket;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class WebSocketAdapter {

    protected Consumer<String> messageCallback;
    protected Action openCallback;
    protected BiConsumer<Integer, String> closeCallback;
    protected Consumer<Throwable> errorCallback;

    public abstract CompletableFuture<Void> connect(String url, Map<String, String> headers);

    public abstract CompletableFuture<Void> send(String message);

    public abstract void close();

    public void onMessage(Consumer<String> callback) {
        this.messageCallback = callback;
    }

    public void onOpen(Action callback) {
        this.openCallback = callback;
    }

    public void onClose(BiConsumer<Integer, String> callback) {
        this.closeCallback = callback;
    }

    public void onError(Consumer<Throwable> errorCallback) {
        this.errorCallback = errorCallback;
    }

}
