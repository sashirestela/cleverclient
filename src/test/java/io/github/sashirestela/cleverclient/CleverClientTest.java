package io.github.sashirestela.cleverclient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.http.HttpClient;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.Test;

import io.github.sashirestela.cleverclient.annotation.GET;

class CleverClientTest {

  @Test
  void shouldSetPropertiesToDefaultValuesWhenBuilderIsCalledWithoutThoseProperties() {
    CleverClient cleverClient = CleverClient.builder()
        .urlBase("https://test")
        .build();
    assertEquals(List.of(), cleverClient.getHeaders());
    assertEquals(HttpClient.Version.HTTP_2, cleverClient.getHttpClient().version());
    assertNotNull(cleverClient.getUrlBase());
    assertNotNull(cleverClient.getHttpProcessor());
  }

  @Test
  void shouldImplementInterfaceWhenCallingCreate() {
    CleverClient cleverClient = CleverClient.builder()
        .urlBase("https://test")
        .build();
    TestCleverClient test = cleverClient.create(TestCleverClient.class);
    assertNotNull(test);
  }

  interface TestCleverClient {
    @GET("/api/")
    CompletableFuture<String> getText();
  }
}
