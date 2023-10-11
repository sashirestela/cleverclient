package io.github.sashirestela.cleverclient.util;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CommonUtil {
  private CommonUtil() {
  }

  private static class SingletonHelper {
    private static final CommonUtil INSTANCE = new CommonUtil();
  }

  public static CommonUtil get() {
    return SingletonHelper.INSTANCE;
  }

  public boolean isNullOrEmpty(Object[] array) {
    return array == null || array.length == 0;
  }

  public boolean isNullOrEmpty(List<?> list) {
    return list == null || list.isEmpty();
  }

  public boolean isNullOrEmpty(String text) {
    return text == null || text.isBlank();
  }

  public List<String> findFullMatches(String text, String regex) {
    var matcher = Pattern.compile(regex).matcher(text);
    return matcher.results()
        .map(mr -> mr.group(1))
        .collect(Collectors.toList());
  }

  public String capitalize(String text) {
    return text.substring(0, 1).toUpperCase() + text.substring(1);
  }

  public <T> T[] concatArrays(T[] array1, T[] array2) {
    var result = Arrays.copyOf(array1, array1.length + array2.length);
    System.arraycopy(array2, 0, result, array1.length, array2.length);
    return result;
  }
}