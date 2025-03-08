package io.github.sashirestela.cleverclient.support;

import io.github.sashirestela.cleverclient.ResponseInfo;
import io.github.sashirestela.cleverclient.util.Constant;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Optional;

/**
 * A custom exception class for handling HTTP client-related errors and providing detailed
 * information about HTTP responses and requests. This exception extends RuntimeException and can be
 * used to capture and represent various HTTP communication errors with additional context.
 */
public class CleverClientException extends RuntimeException {

    /**
     * Stores HTTP response information associated with this exception.
     */
    private final ResponseInfo responseInfo;

    /**
     * Constructs a new CleverClientException with a simple error message.
     *
     * @param message A descriptive error message
     */
    public CleverClientException(String message) {
        super(message);
        this.responseInfo = null;
    }

    /**
     * Constructs a new CleverClientException with a cause.
     *
     * @param cause The original exception that triggered this CleverClientException
     */
    public CleverClientException(Throwable cause) {
        super(cause);
        this.responseInfo = cause instanceof CleverClientException
                ? ((CleverClientException) cause).responseInfo().orElse(null)
                : null;
    }

    /**
     * Constructs a new CleverClientException with a formatted message and a cause.
     *
     * @param message    A message template with placeholders for dynamic values
     * @param parameters An array of parameters to format the message, with the last parameter being the
     *                   original cause
     */
    public CleverClientException(String message, Object... parameters) {
        super(MessageFormat.format(message, Arrays.copyOfRange(parameters, 0, parameters.length - 1)),
                (Throwable) parameters[parameters.length - 1]);
        this.responseInfo = null;
    }

    /**
     * Constructs a new CleverClientException with HTTP response information.
     *
     * @param responseInfo Detailed information about the HTTP response
     */
    public CleverClientException(ResponseInfo responseInfo) {
        super(MessageFormat.format(Constant.HTTP_ERROR_MESSAGE, responseInfo.getStatusCode()), null);
        this.responseInfo = responseInfo;
    }

    /**
     * Retrieves the associated HTTP response information as an Optional.
     *
     * @return An Optional containing HttpResponseInfo if available, otherwise empty
     */
    public Optional<ResponseInfo> responseInfo() {
        return Optional.ofNullable(responseInfo);
    }

}
