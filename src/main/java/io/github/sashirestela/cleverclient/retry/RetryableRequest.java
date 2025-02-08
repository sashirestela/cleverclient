package io.github.sashirestela.cleverclient.retry;

import io.github.sashirestela.cleverclient.support.CleverClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class RetryableRequest {

    private static final Logger logger = LoggerFactory.getLogger(RetryableRequest.class);

    private final RetryConfig config;
    private final Random random;

    public RetryableRequest(RetryConfig config) {
        this.config = config;
        this.random = new SecureRandom();
    }

    public <T> T execute(Supplier<T> operation) {
        int attempt = 1;
        Exception lastException = null;
        while (attempt <= config.getMaxAttempts()) {
            try {
                return operation.get();
            } catch (Exception e) {
                lastException = e;
                if (!isRetryable(e) || attempt == config.getMaxAttempts()) {
                    break;
                }
                long delayMs = calculateDelayWithJitter(attempt);
                logger.debug("Request failed (attempt {}/{}). Retrying in {} ms...",
                        attempt, config.getMaxAttempts(), delayMs);
                try {
                    Thread.sleep(delayMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new CleverClientException(ie);
                }
                attempt++;
            }
        }
        throw new CleverClientException("Request failed after " + config.getMaxAttempts() + " attempts", lastException);
    }

    public <T> CompletableFuture<T> executeAsync(Supplier<Object> operation) {
        CompletableFuture<T> future = new CompletableFuture<>();
        executeAsyncAttempt(operation, 1, future);
        return future;
    }

    @SuppressWarnings("unchecked")
    private <T> void executeAsyncAttempt(Supplier<Object> operation,
            int attempt, CompletableFuture<T> resultFuture) {
        ((CompletableFuture<T>) operation.get()).handle((result, error) -> {
            if (error == null) {
                resultFuture.complete(result);
                return null;
            }

            if (!isRetryable(error) || attempt >= config.getMaxAttempts()) {
                resultFuture.completeExceptionally(new CleverClientException(
                        "Request failed after " + attempt + " attempts", error));
                return null;
            }

            long delayMs = calculateDelayWithJitter(attempt);
            logger.debug("Async request failed (attempt {}/{}). Retrying in {} ms...",
                    attempt, config.getMaxAttempts(), delayMs);

            CompletableFuture.delayedExecutor(delayMs, TimeUnit.MILLISECONDS)
                    .execute(() -> executeAsyncAttempt(operation, attempt + 1, resultFuture));
            return null;
        });
    }

    private boolean isRetryable(Throwable exception) {
        exception = (exception instanceof CompletionException) ? exception.getCause() : exception;
        if (exception == null)
            return false;
        Throwable theException = (exception instanceof CleverClientException) ? exception.getCause() : exception;
        if (theException != null) {
            return Arrays.stream(config.getRetryableExceptions())
                    .anyMatch(retryable -> retryable.isInstance(theException));
        } else {
            return ((CleverClientException) exception).responseInfo()
                    .map(respInfo -> Arrays.stream(config.getRetryableStatusCodes())
                            .anyMatch(range -> range.length == 1
                                    ? respInfo.getStatusCode() == range[0]
                                    : respInfo.getStatusCode() >= range[0] && respInfo.getStatusCode() <= range[1]))
                    .orElse(false);
        }
    }

    private long calculateDelayWithJitter(int attempt) {
        double multiplier = Math.pow(config.getBackoffMultiplier(), attempt - 1.0);
        long baseDelay = (long) (config.getInitialDelayMs() * multiplier);
        baseDelay = Math.min(baseDelay, config.getMaxDelayMs());
        if (config.getJitterFactor() > 0) {
            long jitterRange = (long) (baseDelay * config.getJitterFactor());
            // Generate random jitter between -jitterRange/2 and +jitterRange/2
            long halfRange = jitterRange / 2;
            long jitter = -halfRange + (random.nextLong() % (halfRange + 1));
            baseDelay = Math.max(0, baseDelay + jitter);
            // Ensure we don't exceed maxDelayMs even with jitter
            baseDelay = Math.min(baseDelay, config.getMaxDelayMs());
        }
        return baseDelay;
    }

}
