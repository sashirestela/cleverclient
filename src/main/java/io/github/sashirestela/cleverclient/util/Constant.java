package io.github.sashirestela.cleverclient.util;

import java.math.BigInteger;
import java.util.List;
import java.util.Random;

public class Constant {

    private Constant() {
    }

    public static final List<String> HTTP_METHODS = List.of("GET", "POST", "PUT", "DELETE");
    public static final List<String> PARAMETER_TYPES = List.of("Path", "Query", "Body");
    public static final List<String> MULTIPART_AS_LIST = List.of("Multipart");

    public static final String JSON_EMPTY_CLASS = "{\"type\":\"object\",\"properties\":{}}";

    public static final String HEADER_CONTENT_TYPE = "Content-Type";

    public static final String TYPE_APP_JSON = "application/json";
    public static final String TYPE_MULTIPART = "multipart/form-data";

    public static final String BOUNDARY_TITLE = "; boundary=";
    public static final String BOUNDARY_VALUE = new BigInteger(256, new Random()).toString();

    public static final String DEF_ANNOT_ATTRIB = "value";

    public static final String REGEX_PATH_PARAM_URL = "\\{(.*?)\\}";

}