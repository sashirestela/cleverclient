package io.github.sashirestela.cleverclient.retry;

import lombok.Builder;
import lombok.Getter;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;

@Getter
@Builder
public class RetryConfig {

    @Builder.Default
    private int maxAttempts = 3;

    @Builder.Default
    private long initialDelayMs = 1000;

    @Builder.Default
    private long maxDelayMs = 10000;

    @Builder.Default
    private double backoffMultiplier = 2.0;

    @Builder.Default
    private double jitterFactor = 0.2;

    @Builder.Default
    private Class<? extends Throwable>[] retryableExceptions = new Class[] {
            IOException.class,
            ConnectException.class,
            SocketTimeoutException.class
    };

    @Builder.Default
    private int[][] retryableStatusCodes = {
            { 408, 409 },
            { 429 },
            { 500, 599 }
    };

    public static RetryConfig of() {
        return RetryConfig.builder().build();
    }

}
