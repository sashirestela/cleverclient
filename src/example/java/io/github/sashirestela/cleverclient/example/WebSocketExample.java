package io.github.sashirestela.cleverclient.example;

import io.github.sashirestela.cleverclient.websocket.JavaHttpWebSocketAdapter;
import io.github.sashirestela.cleverclient.websocket.WebSocketAdapter;

import java.util.Map;

public class WebSocketExample {

    protected WebSocketAdapter webSocketAdapter;

    public WebSocketExample() {
        this.webSocketAdapter = new JavaHttpWebSocketAdapter();
    }

    public void run() {
        final String BASE_URL = "wss://s13970.blr1.piesocket.com/v3/1?api_key=" + System.getenv("PIESOCKET_API_KEY")
                + "&notify_self=1";

        webSocketAdapter.onOpen(() -> System.out.println("Connected"));
        webSocketAdapter.onMessage(message -> System.out.println("Received: " + message));
        webSocketAdapter.onClose((code, message) -> System.out.println("Closed"));

        webSocketAdapter.connect(BASE_URL, Map.of()).join();
        webSocketAdapter.send("Hello World!").join();
        webSocketAdapter.send("Welcome to the Jungle!").join();
        webSocketAdapter.close();
    }

    public static void main(String[] args) {
        var example = new WebSocketExample();
        example.run();
    }

}
