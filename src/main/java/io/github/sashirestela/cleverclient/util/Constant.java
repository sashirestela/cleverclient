package io.github.sashirestela.cleverclient.util;

import java.math.BigInteger;
import java.security.SecureRandom;

public class Constant {

    private Constant() {
    }

    public static final String BOUNDARY_VALUE = new BigInteger(256, new SecureRandom()).toString();

    public static final String REGEX_PATH_PARAM_URL = "\\{(.*?)\\}";

    public static final String HTTP_ERROR_MESSAGE = "HTTP interaction failed: server returned a {0} response status.";

    public static final int HTTP_CLIENT_ERROR_CODE = 400;

    public static final int HTTP_SERVER_ERROR_CODE = 500;

}
