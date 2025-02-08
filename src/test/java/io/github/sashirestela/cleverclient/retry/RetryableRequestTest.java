package io.github.sashirestela.cleverclient.retry;

import io.github.sashirestela.cleverclient.ResponseInfo;
import io.github.sashirestela.cleverclient.support.CleverClientException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RetryableRequestTest {

    @Mock
    private Supplier<String> mockOperation;

    @Mock
    private Supplier<Object> mockAsyncOperation;

    private RetryConfig defaultConfig;
    private RetryableRequest retryableRequest;

    @BeforeEach
    void setup() {
        defaultConfig = RetryConfig.builder()
                .maxAttempts(3)
                .initialDelayMs(100)
                .maxDelayMs(1000)
                .build();
        retryableRequest = new RetryableRequest(defaultConfig);
    }

    @Test
    void execute_SucceedsFirstTry() {
        when(mockOperation.get()).thenReturn("success");

        String result = retryableRequest.execute(mockOperation);

        assertEquals("success", result);
        verify(mockOperation, times(1)).get();
    }

    @Test
    void execute_SucceedsAfterRetries() {
        when(mockOperation.get())
                .thenThrow(new CleverClientException(new IOException("Network error")))
                .thenThrow(new CleverClientException(new IOException("Network error")))
                .thenReturn("success");

        String result = retryableRequest.execute(mockOperation);

        assertEquals("success", result);
        verify(mockOperation, times(3)).get();
    }

    @Test
    void execute_ExhaustsRetries() {
        when(mockOperation.get())
                .thenThrow(new CleverClientException(new IOException("Network error")));

        CleverClientException exception = assertThrows(
                CleverClientException.class,
                () -> retryableRequest.execute(mockOperation));

        assertTrue(exception.getMessage().contains("3 attempts"));
        verify(mockOperation, times(3)).get();
    }

    @Test
    void execute_NonRetryableException() {
        when(mockOperation.get())
                .thenThrow(new IllegalArgumentException("Bad input"));

        CleverClientException exception = assertThrows(
                CleverClientException.class,
                () -> retryableRequest.execute(mockOperation));

        assertTrue(exception.getCause() instanceof IllegalArgumentException);
        verify(mockOperation, times(1)).get();
    }

    @Test
    void execute_RetryableStatusCodes() {
        ResponseInfo responseInfo = ResponseInfo.builder()
                .statusCode(503)
                .build();

        when(mockOperation.get())
                .thenThrow(new CleverClientException(responseInfo))
                .thenReturn("success");

        String result = retryableRequest.execute(mockOperation);

        assertEquals("success", result);
        verify(mockOperation, times(2)).get();
    }

    @Test
    void executeAsync_SucceedsFirstTry() {
        CompletableFuture<String> future = CompletableFuture.completedFuture("success");
        when(mockAsyncOperation.get()).thenReturn(future);

        CompletableFuture<String> result = retryableRequest.executeAsync(mockAsyncOperation);

        assertNotNull(result);
        assertEquals("success", result.join());
        verify(mockAsyncOperation, times(1)).get();
    }

    @Test
    void executeAsync_SucceedsAfterRetries() {
        CompletableFuture<String> failedFuture1 = new CompletableFuture<>();
        failedFuture1.completeExceptionally(new ConnectException("Connection refused"));

        CompletableFuture<String> failedFuture2 = new CompletableFuture<>();
        failedFuture2.completeExceptionally(new ConnectException("Connection refused"));

        CompletableFuture<String> successFuture = CompletableFuture.completedFuture("success");

        when(mockAsyncOperation.get())
                .thenReturn(failedFuture1)
                .thenReturn(failedFuture2)
                .thenReturn(successFuture);

        CompletableFuture<String> result = retryableRequest.executeAsync(mockAsyncOperation);

        assertNotNull(result);
        assertEquals("success", result.join());
        verify(mockAsyncOperation, times(3)).get();
    }

    @Test
    void executeAsync_ExhaustsRetries() {
        CompletableFuture<String> failedFuture1 = new CompletableFuture<>();
        failedFuture1.completeExceptionally(new IOException("Network error 1"));
        CompletableFuture<String> failedFuture2 = new CompletableFuture<>();
        failedFuture2.completeExceptionally(new IOException("Network error 2"));
        CompletableFuture<String> failedFuture3 = new CompletableFuture<>();
        failedFuture3.completeExceptionally(new IOException("Network error 3"));

        when(mockAsyncOperation.get())
                .thenReturn(failedFuture1)
                .thenReturn(failedFuture2)
                .thenReturn(failedFuture3);

        CompletableFuture<String> result = retryableRequest.executeAsync(mockAsyncOperation);
        ExecutionException exception = assertThrows(
                ExecutionException.class,
                () -> result.get(1, TimeUnit.SECONDS));

        assertTrue(exception.getCause() instanceof CleverClientException);
        CleverClientException clientException = (CleverClientException) exception.getCause();
        assertTrue(clientException.getMessage().contains("3 attempts"));
        assertTrue(clientException.getCause() instanceof IOException);
        assertEquals("Network error 3", clientException.getCause().getMessage());
        verify(mockAsyncOperation, times(3)).get();
    }

    @SuppressWarnings("unchecked")
    @Test
    void execute_CustomRetryConfig() {
        RetryConfig customConfig = RetryConfig.builder()
                .maxAttempts(2)
                .initialDelayMs(100)
                .maxDelayMs(500)
                .backoffMultiplier(1.5)
                .jitterFactor(0.1)
                .retryableExceptions(new Class[] { SocketTimeoutException.class })
                .retryableStatusCodes(new int[][] { { 503 } })
                .build();

        RetryableRequest customRetry = new RetryableRequest(customConfig);

        when(mockOperation.get())
                .thenThrow(new CleverClientException(new SocketTimeoutException("Timeout")))
                .thenReturn("success");

        String result = customRetry.execute(mockOperation);

        assertEquals("success", result);
        verify(mockOperation, times(2)).get();
    }

}
