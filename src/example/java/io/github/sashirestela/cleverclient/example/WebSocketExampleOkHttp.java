package io.github.sashirestela.cleverclient.example;

import io.github.sashirestela.cleverclient.websocket.OkHttpWebSocketAdapter;

public class WebSocketExampleOkHttp extends WebSocketExample {

    public WebSocketExampleOkHttp() {
        this.webSocketAdapter = new OkHttpWebSocketAdapter();
    }

    public static void main(String[] args) {
        var example = new WebSocketExampleOkHttp();
        example.run();
    }

}
