package io.github.sashirestela.cleverclient.example.util;

import io.github.sashirestela.cleverclient.ExceptionConverter;
import io.github.sashirestela.cleverclient.ResponseInfo;
import lombok.Getter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

public class Commons {

    private Commons() {
    }

    public static void redirectSystemErr() throws FileNotFoundException {
        File file = new File("error.log");
        FileOutputStream fos = new FileOutputStream(file);
        PrintStream ps = new PrintStream(fos);
        System.setErr(ps);
    }

    public static class MyExceptionConverter extends ExceptionConverter {

        private MyExceptionConverter() {
        }

        public static void rethrow(Throwable e) {
            throw new MyExceptionConverter().convert(e);
        }

        @Override
        public RuntimeException convertHttpException(ResponseInfo responseInfo) {
            return new MyHttpException(responseInfo.getStatusCode(), responseInfo.getData());
        }

    }

    @Getter
    public static class MyHttpException extends RuntimeException {

        private final int responseCode;
        private final String errorDetail;

        public MyHttpException(int responseCode, String errorDetail) {
            this.responseCode = responseCode;
            this.errorDetail = errorDetail;
        }

    }

}
