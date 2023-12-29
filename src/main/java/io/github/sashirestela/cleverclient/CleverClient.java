package io.github.sashirestela.cleverclient;

import java.net.http.HttpClient;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.sashirestela.cleverclient.http.HttpProcessor;
import io.github.sashirestela.cleverclient.support.CleverClientException;
import io.github.sashirestela.cleverclient.support.CleverClientSSE;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;

/**
 * Main class and entry point to use this library. This is a kind of wrapper
 * that makes it easier to use the Java's HttpClient component to call http
 * services by using annotated interfaces.
 */
@Getter
public class CleverClient {
    private static Logger logger = LoggerFactory.getLogger(CleverClient.class);

    private String urlBase;
    private List<String> headers;
    private HttpClient httpClient;
    private HttpProcessor httpProcessor;

    /**
     * Constructor to create an instance of CleverClient.
     * 
     * @param urlBase     Root of the url of the API service to call. Mandatory.
     * @param headers     Http headers for all the API service. Header's name and
     *                    value must be individual entries in the list. Optional.
     * @param httpClient  Custom Java's HttpClient component. One is created by
     *                    default if none is passed. Optional.
     * @param endOfStream Text used to mark the final of streams when handling
     *                    server sent events (SSE). Optional.
     */
    @Builder
    public CleverClient(@NonNull String urlBase, @Singular List<String> headers, HttpClient httpClient,
            String endOfStream) {
        this.urlBase = urlBase;
        this.headers = Optional.ofNullable(headers).orElse(List.of());
        if (this.headers.size() % 2 > 0) {
            throw new CleverClientException("Headers must be entered as pair of values in the list.", null, null);
        }
        this.httpClient = Optional.ofNullable(httpClient).orElse(HttpClient.newHttpClient());
        CleverClientSSE.setEndOfStream(endOfStream);
        this.httpProcessor = new HttpProcessor(this.urlBase, this.headers, this.httpClient);
        logger.debug("CleverClient has been created.");
    }

    /**
     * Creates an instance of an annotated interface that represents a resource of
     * the API service and its methods represent the endpoints that we can call:
     * Get, Post, Put, Patch, Delete.
     * 
     * @param <T>            Type of the interface.
     * @param interfaceClass The interface to be instanced.
     * @return A proxy instance of the interface.
     */
    public <T> T create(Class<T> interfaceClass) {
        return this.httpProcessor.createProxy(interfaceClass);
    }
}