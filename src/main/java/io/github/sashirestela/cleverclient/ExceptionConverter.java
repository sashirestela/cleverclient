package io.github.sashirestela.cleverclient;

import io.github.sashirestela.cleverclient.support.CleverClientException;

public abstract class ExceptionConverter {

    public RuntimeException convert(Throwable exception) {
        var ccException = new CleverClientException(exception);
        if (exception instanceof CleverClientException) {
            ccException = (CleverClientException) exception;
        } else if (exception.getCause() instanceof CleverClientException) {
            ccException = (CleverClientException) exception.getCause();
        }
        var optionalResponseInfo = ccException.responseInfo();
        if (optionalResponseInfo.isPresent()) {
            return convertHttpException(optionalResponseInfo.get());
        } else {
            return (RuntimeException) exception;
        }
    }

    public abstract RuntimeException convertHttpException(ResponseInfo responseInfo);

}
