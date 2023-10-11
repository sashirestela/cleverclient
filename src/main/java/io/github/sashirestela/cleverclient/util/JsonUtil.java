package io.github.sashirestela.cleverclient.util;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;

import io.github.sashirestela.cleverclient.support.CleverClientException;

public class JsonUtil {
  private ObjectMapper objectMapper;

  private JsonUtil() {
    objectMapper = new ObjectMapper();
  }

  private static class SingletonHelper {
    private static final JsonUtil INSTANCE = new JsonUtil();
  }

  public static JsonUtil get() {
    return SingletonHelper.INSTANCE;
  }

  public <T> String objectToJson(T object) {
    try {
      return objectMapper.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      throw new CleverClientException("Cannot convert the object {0} to Json.", object, e);
    }
  }

  public <T> T jsonToObject(String json, Class<T> clazz) {
    try {
      return objectMapper.readValue(json, clazz);
    } catch (JsonProcessingException e) {
      throw new CleverClientException("Cannot convert the Json {0} to class {1}.", json, clazz.getName(), e);
    }
  }

  public <T> List<T> jsonToList(String json, Class<T> clazz) {
    try {
      CollectionType listType = objectMapper.getTypeFactory().constructCollectionType(ArrayList.class, clazz);
      return objectMapper.readValue(json, listType);
    } catch (JsonProcessingException e) {
      throw new CleverClientException("Cannot convert the Json {0} to List of {1}.", json, clazz.getName(), e);
    }
  }

  public <T, U> T jsonToParametricObject(String json, Class<T> clazzT, Class<U> clazzU) {
    try {
      JavaType javaType = objectMapper.getTypeFactory().constructParametricType(clazzT, clazzU);
      return objectMapper.readValue(json, javaType);
    } catch (JsonProcessingException e) {
      throw new CleverClientException("Cannot convert the Json {0} to class of {1}.", json, clazzT.getName(), e);
    }
  }
}