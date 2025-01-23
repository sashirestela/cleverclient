package io.github.sashirestela.cleverclient.example;

import io.github.sashirestela.cleverclient.CleverClient;
import io.github.sashirestela.cleverclient.websocket.JavaHttpWebSocketAdapter;
import io.github.sashirestela.cleverclient.websocket.WebSocketAdapter;

public class WebSocketExample {

    protected WebSocketAdapter webSocketAdapter;

    public WebSocketExample() {
        this.webSocketAdapter = new JavaHttpWebSocketAdapter();
    }

    public void run() {
        var webSocket = CleverClient.WebSocket.builder()
                .baseUrl("wss://s13970.blr1.piesocket.com/v3/1")
                .queryParam("api_key", System.getenv("PIESOCKET_API_KEY"))
                .queryParam("notify_self", "1")
                .webSockewAdapter(webSocketAdapter)
                .build();

        webSocket.onOpen(() -> System.out.println("Connected"));
        webSocket.onMessage(message -> System.out.println("Received: " + message));
        webSocket.onClose((code, message) -> System.out.println("Closed"));

        webSocket.connect().join();
        webSocket.send("Hello World!").join();
        webSocket.send("Welcome to the Jungle!").join();
        webSocket.close();
    }

    public static void main(String[] args) {
        var example = new WebSocketExample();
        example.run();
    }

}
