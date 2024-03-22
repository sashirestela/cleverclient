package io.github.sashirestela.cleverclient;

import io.github.sashirestela.cleverclient.http.HttpProcessor;
import io.github.sashirestela.cleverclient.http.HttpRequestData;
import io.github.sashirestela.cleverclient.support.Configurator;
import io.github.sashirestela.cleverclient.util.CommonUtil;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.HttpClient;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;

/**
 * Main class and entry point to use this library. This is a kind of wrapper that makes it easier to
 * use the Java's HttpClient component to call http services by using annotated interfaces.
 */
@Getter
public class CleverClient {

    private static final Logger logger = LoggerFactory.getLogger(CleverClient.class);

    private final String baseUrl;
    private final Map<String, String> headers;
    private final HttpClient httpClient;
    private final UnaryOperator<HttpRequestData> requestInterceptor;
    private final HttpProcessor httpProcessor;

    /**
     * Constructor to create an instance of CleverClient.
     * 
     * @param baseUrl            Root of the url of the API service to call. Mandatory.
     * @param headers            Http headers for all the API service. Optional.
     * @param httpClient         Custom Java's HttpClient component. One is created by default if none
     *                           is passed. Optional.
     * @param requestInterceptor Function to modify the request once it has been built.
     * @param endsOfStream       Texts used to mark the final of streams when handling server sent
     *                           events (SSE). Optional.
     */
    @Builder
    public CleverClient(@NonNull String baseUrl, @Singular Map<String, String> headers, HttpClient httpClient,
            UnaryOperator<HttpRequestData> requestInterceptor, @Singular("endOfStream") List<String> endsOfStream) {
        this.baseUrl = baseUrl;
        this.headers = Optional.ofNullable(headers).orElse(Map.of());
        this.httpClient = Optional.ofNullable(httpClient).orElse(HttpClient.newHttpClient());
        this.requestInterceptor = requestInterceptor;
        this.httpProcessor = HttpProcessor.builder()
                .baseUrl(this.baseUrl)
                .headers(CommonUtil.mapToListOfString(this.headers))
                .httpClient(this.httpClient)
                .requestInterceptor(this.requestInterceptor)
                .build();
        Configurator.builder()
                .endsOfStream(Optional.ofNullable(endsOfStream).orElse(Arrays.asList()))
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

}
