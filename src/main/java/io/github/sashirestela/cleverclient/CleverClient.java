package io.github.sashirestela.cleverclient;

import java.net.http.HttpClient;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.sashirestela.cleverclient.http.HttpProcessor;
import io.github.sashirestela.cleverclient.http.InvocationFilter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor
public class CleverClient {
  private static Logger logger = LoggerFactory.getLogger(CleverClient.class);

  @NonNull
  private HttpClient httpClient;

  @NonNull
  private String urlBase;

  private List<String> headers;
  private HttpProcessor httpProcessor;

  @Builder
  public CleverClient(HttpClient httpClient, String urlBase, List<String> headers) {
    this.httpClient = httpClient;
    this.urlBase = urlBase;
    this.headers = headers;
    this.httpProcessor = new HttpProcessor(this.httpClient, this.urlBase, this.headers);
    logger.debug("CleverClient has been created.");
  }

  public <T> T create(Class<T> interfaceClass) {
    return httpProcessor.createProxy(interfaceClass, null);
  }

  public <T> T create(Class<T> interfaceClass, InvocationFilter filter) {
    return httpProcessor.createProxy(interfaceClass, filter);
  }
}