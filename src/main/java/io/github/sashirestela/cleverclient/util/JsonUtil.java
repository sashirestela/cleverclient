package io.github.sashirestela.cleverclient.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.type.CollectionType;
import io.github.sashirestela.cleverclient.support.CleverClientException;
import io.github.sashirestela.cleverclient.support.Configurator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JsonUtil {

    private static final ObjectMapper objectMapperStrict = Configurator.one().getObjectMapper();

    private static final ObjectReader objectReaderIgnoringUnknown = objectMapperStrict.reader()
            .without(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    private JsonUtil() {
    }

    public static <T> Map<String, Object> objectToMap(T object) {
        try {
            return objectMapperStrict.convertValue(object, new TypeReference<>() {
            });
        } catch (IllegalArgumentException e) {
            throw new CleverClientException("Cannot convert object {0} to Map.", object, e);
        }
    }

    public static <T> String objectToJson(T object) {
        try {
            return objectMapperStrict.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new CleverClientException("Cannot convert the object {0} to Json.", object, e);
        }
    }

    public static <T> T jsonToObject(String json, Class<T> clazz) {
        try {
            return objectReaderIgnoringUnknown.readValue(json, clazz);
        } catch (IOException e) {
            throw new CleverClientException("Cannot convert the Json {0} to class {1}.", json, clazz.getName(), e);
        }
    }

    public static <T> List<T> jsonToList(String json, Class<T> clazz) {
        try {
            CollectionType listType = objectReaderIgnoringUnknown.getTypeFactory()
                    .constructCollectionType(ArrayList.class, clazz);
            return objectReaderIgnoringUnknown.forType(listType).readValue(json);
        } catch (JsonProcessingException e) {
            throw new CleverClientException("Cannot convert the Json {0} to List of {1}.", json, clazz.getName(), e);
        }
    }

    public static <T, U> T jsonToParametricObject(String json, Class<T> clazzT, Class<U> clazzU) {
        try {
            JavaType javaType = objectReaderIgnoringUnknown.getTypeFactory()
                    .constructParametricType(clazzT, clazzU);
            return objectReaderIgnoringUnknown.forType(javaType).readValue(json);
        } catch (JsonProcessingException e) {
            throw new CleverClientException("Cannot convert the Json {0} to class of {1}.", json, clazzT.getName(), e);
        }
    }

}
