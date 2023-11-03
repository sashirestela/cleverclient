package io.github.sashirestela.cleverclient;

import java.net.http.HttpClient;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.sashirestela.cleverclient.http.HttpProcessor;
import io.github.sashirestela.cleverclient.support.CleverClientSSE;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class CleverClient {
    private static Logger logger = LoggerFactory.getLogger(CleverClient.class);

    @NonNull
    private String urlBase;

    private List<String> headers;
    private HttpClient httpClient;

    private HttpProcessor httpProcessor;

    @Builder
    public CleverClient(String urlBase, List<String> headers, HttpClient httpClient, String endOfStream) {
        this.urlBase = urlBase;
        this.headers = Optional.ofNullable(headers).orElse(List.of());
        this.httpClient = Optional.ofNullable(httpClient).orElse(HttpClient.newHttpClient());
        this.httpProcessor = new HttpProcessor(this.httpClient, this.urlBase, this.headers);
        CleverClientSSE.setEndOfStream(endOfStream);
        logger.debug("CleverClient has been created.");
    }

    public <T> T create(Class<T> interfaceClass) {
        return httpProcessor.createProxy(interfaceClass);
    }
}