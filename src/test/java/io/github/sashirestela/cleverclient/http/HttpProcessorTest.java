package io.github.sashirestela.cleverclient.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.HttpURLConnection;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.sashirestela.cleverclient.support.CleverClientException;

@SuppressWarnings("unchecked")
class HttpProcessorTest {

  HttpProcessor httpProcessor;
  HttpClient httpClient = mock(HttpClient.class);
  HttpResponse<String> httpResponse = mock(HttpResponse.class);
  HttpResponse<Stream<String>> httpResponseStream = mock(HttpResponse.class);

  @BeforeEach
  void init() {
    httpProcessor = new HttpProcessor(
        httpClient,
        "https://api.demmo",
        null);
  }

  @Test
  void shouldThownExceptionWhenCallingCreateMethodForNoAnnotedMethod() {
    Exception exception = assertThrows(CleverClientException.class,
        () -> httpProcessor.createProxy(ITest.NotAnnotatedService.class, null));
    assertTrue(exception.getMessage().contains("Missing HTTP anotation for the method"));
  }

  @Test
  void shouldThownExceptionWhenCallingCreateMethodForBadPathParamMethod() {
    Exception exception = assertThrows(CleverClientException.class,
        () -> httpProcessor.createProxy(ITest.BadPathParamService.class, null));
    assertTrue(exception.getMessage().contains("Path param demoId in the url cannot find"));
  }

  @Test
  void shouldThownExceptionWhenCallingMethodReturnTypeIsUnsupported() {
    var service = httpProcessor.createProxy(ITest.GoodService.class, null);
    Exception exception = assertThrows(CleverClientException.class,
        () -> service.unsupportedMethod());
    assertTrue(exception.getMessage().contains("Unsupported return type"));
  }

  @Test
  void shouldReturnAStringWhenMethodReturnTypeIsAString() {
    when(httpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandlers.ofString().getClass())))
        .thenReturn(CompletableFuture.completedFuture(httpResponse));
    when(httpResponse.statusCode()).thenReturn(HttpURLConnection.HTTP_OK);
    when(httpResponse.body()).thenReturn("{\"id\":100,\"description\":\"Description\",\"active\":true}");
    
    var service = httpProcessor.createProxy(ITest.GoodService.class, null);
    var actualDemo = service.getDemoPlain(100).join();
    var expectedDemo = "{\"id\":100,\"description\":\"Description\",\"active\":true}";
    
    assertEquals(expectedDemo, actualDemo);
  }

  @Test
  void shouldReturnAnObjectWhenMethodReturnTypeIsAnObject() {
    when(httpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandlers.ofString().getClass())))
        .thenReturn(CompletableFuture.completedFuture(httpResponse));
    when(httpResponse.statusCode()).thenReturn(HttpURLConnection.HTTP_OK);
    when(httpResponse.body()).thenReturn("{\"id\":100,\"description\":\"Description\",\"active\":true}");
    
    var service = httpProcessor.createProxy(ITest.GoodService.class, null);
    var actualDemo = service.getDemo(100).join();
    var expectedDemo = new ITest.Demo(100, "Description", true);
    
    assertEquals(expectedDemo, actualDemo);
  }

  @Test
  void shouldReturnAGenericWhenMethodReturnTypeIsAnObject() {
    when(httpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandlers.ofString().getClass())))
        .thenReturn(CompletableFuture.completedFuture(httpResponse));
    when(httpResponse.statusCode()).thenReturn(HttpURLConnection.HTTP_OK);
    when(httpResponse.body()).thenReturn("{\"id\":1,\"listDemo\":[{\"id\":100,\"description\":\"Description\",\"active\":true}]}");
    
    var service = httpProcessor.createProxy(ITest.GoodService.class, null);
    var actualGenericDemo = service.getGenericDemo(1).join();
    var actualDemo = actualGenericDemo.getListDemo().get(0);
    var expectedDemo = new ITest.Demo(100, "Description", true);
    
    assertEquals(expectedDemo, actualDemo);
  }

  @Test
  void shouldReturnAListWhenMethodReturnTypeIsAList() {
    when(httpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandlers.ofString().getClass())))
        .thenReturn(CompletableFuture.completedFuture(httpResponse));
    when(httpResponse.statusCode()).thenReturn(HttpURLConnection.HTTP_OK);
    when(httpResponse.body()).thenReturn("[{\"id\":100,\"description\":\"Description\",\"active\":true}]");

    var service = httpProcessor.createProxy(ITest.GoodService.class, null);
    var actualListDemo = service.getDemos().join();
    var actualDemo = actualListDemo.get(0);
    var expectedDemo = new ITest.Demo(100, "Description", true);
    
    assertEquals(expectedDemo, actualDemo);
  }

  @Test
  void shouldReturnAStreamWhenMethodReturnTypeIsAStream() {
    when(httpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandlers.ofLines().getClass())))
        .thenReturn(CompletableFuture.completedFuture(httpResponseStream));
    when(httpResponseStream.statusCode()).thenReturn(HttpURLConnection.HTTP_OK);
    when(httpResponseStream.body()).thenReturn(Stream.of("data: {\"id\":100,\"description\":\"Description\",\"active\":true}"));

    var service = httpProcessor.createProxy(ITest.GoodService.class, null);
    var actualStreamDemo = service.getDemoStream(new ITest.RequestDemo("Descr")).join();
    var actualDemo = actualStreamDemo.findFirst().get();
    var expectedDemo = new ITest.Demo(100, "Description", true);
    
    assertEquals(expectedDemo, actualDemo);
  }

  @Test
  void shouldThrownExceptionWhenCallingNoStreamingMethodAndServerRespondsWithError() {
    when(httpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandlers.ofString().getClass())))
        .thenReturn(CompletableFuture.completedFuture(httpResponse));
    when(httpResponse.statusCode()).thenReturn(HttpURLConnection.HTTP_NOT_FOUND);
    when(httpResponse.body()).thenReturn(
      "{\"error\": {\"message\": \"The resource does not exist\", \"type\": \"T\", \"param\": \"P\", \"code\": \"C\"}}");
    
    var service = httpProcessor.createProxy(ITest.GoodService.class, null);
    var futureService = service.getDemo(100);
    Exception exception = assertThrows(CompletionException.class,
        () -> futureService.join());
    assertTrue(exception.getMessage().contains("The resource does not exist"));
  }

  @Test
  void shouldThrownExceptionWhenCallingStreamingMethodAndServerRespondsWithError() {
    when(httpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandlers.ofLines().getClass())))
        .thenReturn(CompletableFuture.completedFuture(httpResponseStream));
    when(httpResponseStream.statusCode()).thenReturn(HttpURLConnection.HTTP_NOT_FOUND);
    when(httpResponseStream.body()).thenReturn(Stream.of(
      "{\"error\": {\"message\": \"The resource does not exist\", \"type\": \"T\", \"param\": \"P\", \"code\": \"C\"}}"));
    
    var service = httpProcessor.createProxy(ITest.GoodService.class, null);
    var futureService = service.getDemoStream(new ITest.RequestDemo("Descr"));
    Exception exception = assertThrows(CompletionException.class,
        () -> futureService.join());
    assertTrue(exception.getMessage().contains("The resource does not exist"));
  }

  @Test
  void shouldExecuteDefaultMethodWhenItIsCalled() {
    var service = httpProcessor.createProxy(ITest.GoodService.class, null);
    var actualValue = service.defaultMethod("Test");
    var expectedValue = "Hello Test";
    
    assertEquals(expectedValue, actualValue);
  }
}