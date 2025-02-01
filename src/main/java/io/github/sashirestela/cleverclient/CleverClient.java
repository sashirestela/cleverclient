package io.github.sashirestela.cleverclient;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.sashirestela.cleverclient.client.HttpClientAdapter;
import io.github.sashirestela.cleverclient.client.JavaHttpClientAdapter;
import io.github.sashirestela.cleverclient.http.HttpProcessor;
import io.github.sashirestela.cleverclient.http.HttpRequestData;
import io.github.sashirestela.cleverclient.http.HttpResponseData;
import io.github.sashirestela.cleverclient.support.Configurator;
import io.github.sashirestela.cleverclient.util.CommonUtil;
import io.github.sashirestela.cleverclient.websocket.Action;
import io.github.sashirestela.cleverclient.websocket.JavaHttpWebSocketAdapter;
import io.github.sashirestela.cleverclient.websocket.WebSocketAdapter;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

/**
 * Main class and entry point to use this library. This is a smart wrapper that makes it easier to
 * use a Http client component to call http services by using annotated interfaces.
 */
@Getter
public class CleverClient {

    private static final Logger logger = LoggerFactory.getLogger(CleverClient.class);

    private final String baseUrl;
    private final Map<String, String> headers;
    private final Consumer<Object> bodyInspector;
    private final UnaryOperator<HttpRequestData> requestInterceptor;
    private final UnaryOperator<HttpResponseData> responseInterceptor;
    private final HttpClientAdapter clientAdapter;
    private final HttpProcessor httpProcessor;

    /**
     * Constructor to create an instance of CleverClient.
     * 
     * @param baseUrl             Root of the url of the API service to call. Mandatory.
     * @param headers             Http headers for all the API service. Optional.
     * @param bodyInspector       Function to inspect the Body request parameter.
     * @param requestInterceptor  Function to modify the request once it has been built.
     * @param responseInterceptor Function to modify the response once it has been received.
     * @param clientAdapter       Component to call http services. If none is passed the
     *                            JavaHttpClientAdapter will be used. Optional.
     * @param endsOfStream        Texts used to mark the final of streams when handling server sent
     *                            events (SSE). Optional.
     * @param objectMapper        Provides Json conversions either to and from objects. Optional.
     */
    @Builder
    @SuppressWarnings("java:S107")
    public CleverClient(@NonNull String baseUrl, @Singular Map<String, String> headers, Consumer<Object> bodyInspector,
            UnaryOperator<HttpRequestData> requestInterceptor, UnaryOperator<HttpResponseData> responseInterceptor,
            HttpClientAdapter clientAdapter, @Singular("endOfStream") List<String> endsOfStream,
            ObjectMapper objectMapper) {
        this.baseUrl = baseUrl;
        this.headers = Optional.ofNullable(headers).orElse(Map.of());
        this.bodyInspector = bodyInspector;
        this.requestInterceptor = requestInterceptor;
        this.responseInterceptor = responseInterceptor;
        this.clientAdapter = Optional.ofNullable(clientAdapter)
                // Lazy evaluation to not fail on devices without support for HttpClient
                .orElseGet(() -> new JavaHttpClientAdapter());
        this.clientAdapter.setRequestInterceptor(this.requestInterceptor);
        this.clientAdapter.setResponseInterceptor(this.responseInterceptor);

        this.httpProcessor = HttpProcessor.builder()
                .baseUrl(this.baseUrl)
                .headers(CommonUtil.mapToListOfString(this.headers))
                .clientAdapter(this.clientAdapter)
                .bodyInspector(this.bodyInspector)
                .build();
        Configurator.builder()
                .endsOfStream(Optional.ofNullable(endsOfStream).orElse(Arrays.asList()))
                .objectMapper(Optional.ofNullable(objectMapper).orElse(new ObjectMapper()))
                .build();
        logger.debug("CleverClient has been created.");
    }

    /**
     * Creates an instance of an annotated interface that represents a resource of the API service and
     * its methods represent the endpoints that we can call: Get, Post, Put, Patch, Delete.
     * 
     * @param <T>            Type of the interface.
     * @param interfaceClass The interface to be instanced.
     * @return A proxy instance of the interface.
     */
    public <T> T create(Class<T> interfaceClass) {
        return this.httpProcessor.createProxy(interfaceClass);
    }

    /**
     * Handles websocket communication.
     */
    @Getter
    public static class WebSocket {

        private final String baseUrl;
        private final Map<String, String> queryParams;
        private final Map<String, String> headers;
        private final WebSocketAdapter webSockewAdapter;
        private String fullUrl;

        /**
         * Constructor to create an instance of CleverClient.WebSocket
         * 
         * @param baseUrl          Root of the url of the WebSocket to call. Mandatory.
         * @param queryParams      Query parameters (key=value) to be added to the baseUrl.
         * @param headers          Http headers to be passed to the WebSocket. Optional.
         * @param webSockewAdapter Component to do web socket interactions. If none is passed the
         *                         JavaHttpWebSocketAdapter will be used. Optional.
         */
        @Builder
        public WebSocket(@NonNull String baseUrl, @Singular Map<String, String> queryParams,
                @Singular Map<String, String> headers, WebSocketAdapter webSockewAdapter) {
            this.baseUrl = baseUrl;
            this.queryParams = Optional.ofNullable(queryParams).orElse(Map.of());
            this.headers = Optional.ofNullable(headers).orElse(Map.of());
            this.webSockewAdapter = Optional.ofNullable(webSockewAdapter)
                    // Lazy evaluation to not fail on devices without support for HttpClient
                    .orElseGet(() -> new JavaHttpWebSocketAdapter());
            this.fullUrl = buildFullUrl();
        }

        private String buildFullUrl() {
            return baseUrl + CommonUtil.stringMapToUrl(queryParams);
        }

        public CompletableFuture<Void> connect() {
            return webSockewAdapter.connect(fullUrl, headers);
        }

        public CompletableFuture<Void> send(String message) {
            return webSockewAdapter.send(message);
        }

        public void close() {
            webSockewAdapter.close();
        }

        public void onMessage(Consumer<String> callback) {
            webSockewAdapter.onMessage(callback);
        }

        public void onOpen(Action callback) {
            webSockewAdapter.onOpen(callback);
        }

        public void onClose(BiConsumer<Integer, String> callback) {
            webSockewAdapter.onClose(callback);
        }

        public void onError(Consumer<Throwable> callback) {
            webSockewAdapter.onError(callback);
        }

    }

}
