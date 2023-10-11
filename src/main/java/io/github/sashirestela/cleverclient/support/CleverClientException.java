package io.github.sashirestela.cleverclient.support;

import java.text.MessageFormat;
import java.util.Arrays;

public class CleverClientException extends RuntimeException {

  public CleverClientException(String message, Object... parameters) {
    super(MessageFormat.format(message, Arrays.copyOfRange(parameters, 0, parameters.length - 1)),
        (Throwable) parameters[parameters.length - 1]);
  }

}