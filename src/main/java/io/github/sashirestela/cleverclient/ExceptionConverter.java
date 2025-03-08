package io.github.sashirestela.cleverclient;

import io.github.sashirestela.cleverclient.support.CleverClientException;

public abstract class ExceptionConverter {

    public RuntimeException convert(Throwable exception) {
        var rootCause = getRootCause(exception);
        var ccException = new CleverClientException(rootCause);
        var optionalResponseInfo = ccException.responseInfo();
        if (optionalResponseInfo.isPresent()) {
            return convertHttpException(optionalResponseInfo.get());
        } else {
            return ccException;
        }
    }

    public abstract RuntimeException convertHttpException(ResponseInfo responseInfo);

    private Throwable getRootCause(Throwable throwable) {
        Throwable rootCause = throwable;
        while (rootCause.getCause() != null) {
            rootCause = rootCause.getCause();
        }
        return rootCause;
    }

}
