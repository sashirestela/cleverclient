package io.github.sashirestela.cleverclient.util;

import java.math.BigInteger;
import java.util.Random;

public class Constant {

    private Constant() {
    }
    public static final String BOUNDARY_VALUE = new BigInteger(256, new Random()).toString();

    public static final String REGEX_PATH_PARAM_URL = "\\{(.*?)\\}";

}