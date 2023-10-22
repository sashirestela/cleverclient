package io.github.sashirestela.cleverclient.util;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CommonUtil {
  private CommonUtil() {
  }

  public static boolean isNullOrEmpty(Object[] array) {
    return array == null || array.length == 0;
  }

  public static boolean isNullOrEmpty(List<?> list) {
    return list == null || list.isEmpty();
  }

  public static boolean isNullOrEmpty(String text) {
    return text == null || text.isBlank();
  }

  public static List<String> findFullMatches(String text, String regex) {
    var matcher = Pattern.compile(regex).matcher(text);
    return matcher.results()
        .map(mr -> mr.group(1))
        .collect(Collectors.toList());
  }

  public static String capitalize(String text) {
    return text.substring(0, 1).toUpperCase() + text.substring(1);
  }

  public static <T> T[] concatArrays(T[] array1, T[] array2) {
    var result = Arrays.copyOf(array1, array1.length + array2.length);
    System.arraycopy(array2, 0, result, array1.length, array2.length);
    return result;
  }

  public static boolean isInHundredsOf(int value, int range) {
    return Math.floor(value / 100.0) * 100 == range;
  }
}