package io.github.sashirestela.cleverclient.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CommonUtil {
    private CommonUtil() {
    }

    public static boolean isNullOrEmpty(Object obj) {
        return obj == null
                || (obj instanceof Map && ((Map<?, ?>) obj).isEmpty())
                || (obj instanceof Collection && ((Collection<?>) obj).isEmpty())
                || (obj instanceof CharSequence && ((CharSequence) obj).length() == 0)
                || (obj instanceof String && ((String) obj).isBlank())
                || (obj.getClass().isArray() && Array.getLength(obj) == 0);
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

    public static Map<String, String> createMapString(String... keyValPairs) {
        if (keyValPairs.length % 2 > 0) {
            throw new IllegalArgumentException("It is expected an even number of elements.");
        }
        Map<String, String> map = new HashMap<>();
        for (var i = 0; i < keyValPairs.length; i += 2) {
            var key = keyValPairs[i];
            if (key == null) {
                throw new IllegalArgumentException("Unexpected null element for key in position " + i + ".");
            }
            var val = keyValPairs[i + 1];
            map.put(key, val);
        }
        return map;
    }

    public static List<String> mapToListOfString(Map<String, String> map) {
        List<String> list = new ArrayList<>();
        for (var entry : map.entrySet()) {
            list.add(entry.getKey());
            list.add(entry.getValue());
        }
        return list;
    }

    public static Map<String, String> listToMapOfString(List<String> list) {
        var array = list.toArray(new String[0]);
        return createMapString(array);
    }
}