package io.github.sashirestela.cleverclient.util;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommonUtilTest {

    @Test
    void shouldReturnTrueWhenObjectIsNullOrEmpty() {
        Object[] testData = {
                null,
                "",
                "  ",
                new Object[0],
                new ArrayList<>(),
                new HashMap<>(),
        };
        for (Object testItem : testData) {
            assertTrue(CommonUtil.isNullOrEmpty(testItem), "Should treat `" + testItem + "` as empty");
        }
    }

    @Test
    void shouldReturnFalseWhenObjectIsNotEmpty() {
        Object[] testData = {
                1,
                "1",
                new Object[1],
                List.of(1),
                Map.of(1, 2),
        };
        for (Object testItem : testData) {
            assertFalse(CommonUtil.isNullOrEmpty(testItem), "Should treat `" + testItem + "` as NOT empty");
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldReturnFullMatchesWhenSomeTextMatchesRegex() {
        Object[][] testData = {
                { "/api/service", Constant.REGEX_PATH_PARAM_URL, Arrays.asList() },
                { "/api/service/{path1}", Constant.REGEX_PATH_PARAM_URL, Arrays.asList("path1") },
                { "/api/service/{path1}/{path2}", Constant.REGEX_PATH_PARAM_URL, Arrays.asList("path1", "path2") }
        };
        for (Object[] data : testData) {
            List<String> actualListMatches = CommonUtil.findFullMatches((String) data[0], (String) data[1]);
            List<String> expectedListMatches = (List<String>) data[2];
            assertEquals(expectedListMatches, actualListMatches);
        }
    }

    @Test
    void shouldReturnCapitalizedTextWhenOneIsPassed() {
        String[][] testData = {
                { "university", "University" },
                { "National", "National" },
                { "g", "G" }
        };
        for (String[] data : testData) {
            String actualText = CommonUtil.capitalize(data[0]);
            String expectedText = data[1];
            assertEquals(expectedText, actualText);
        }
    }

    @Test
    void shouldReturnJoinedArraysWhenTwoArePassed() {
        Object[][][] testData = {
                { { 1, 2, 3 }, { 4, 5, 6 }, { 1, 2, 3, 4, 5, 6 } },
                { { "ab", "cd", "ef", "gh" }, {}, { "ab", "cd", "ef", "gh" } },
                { {}, { "abc", "cde" }, { "abc", "cde" } },
                { {}, {}, {} }
        };
        for (Object[][] data : testData) {
            Object[] actualArray = CommonUtil.concatArrays(data[0], data[1]);
            Object[] expectedArray = data[2];
            assertArrayEquals(expectedArray, actualArray);
        }
    }

    @Test
    void shouldDetectIfSomeValueIsInHundredsOfOneLimit() {
        Object[][] testData = {
                { 199, 200, false },
                { 200, 200, true },
                { 201, 200, true },
                { 299, 200, true },
                { 300, 200, false }
        };
        for (var data : testData) {
            var actualResult = CommonUtil.isInHundredsOf((int) data[0], (int) data[1]);
            var expectedResult = (boolean) data[2];
            assertEquals(expectedResult, actualResult);
        }
    }

    @Test
    void shouldDetectIfSomeValueIsBetweenHundredsOfTwoLimits() {
        Object[][] testData = {
                { 399, 400, 500, false },
                { 400, 400, 500, true },
                { 500, 400, 500, true },
                { 599, 400, 500, true },
                { 600, 400, 500, false }
        };
        for (var data : testData) {
            var actualResult = CommonUtil.isBetweenHundredsOf((int) data[0], (int) data[1], (int) data[2]);
            var expectedResult = (boolean) data[3];
            assertEquals(expectedResult, actualResult);
        }
    }

    @Test
    void shouldCreateMapStringWhenAStringListIsPassed() {
        var expectedMap = new HashMap<String, String>();
        expectedMap.put("key1", "val1");
        expectedMap.put("key2", "val2");
        var actualMap = CommonUtil.createMapString("key1", "val1", "key2", "val2");
        assertEquals(expectedMap, actualMap);
    }

    @Test
    void shouldThrownExceptionWhenCreatingMapStringAndExistsWrongCondition() {
        String[][] testData = {
                { "key1", "val1", "key2" },
                { "key1", "val1", null, "val2" }
        };
        for (String[] data : testData) {
            assertThrows(IllegalArgumentException.class, () -> CommonUtil.createMapString(data));
        }
    }

    @Test
    void shouldConvertMapToListOfStringWhenAMapIsPassed() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("key1", "val1");
        map.put("key2", "val2");
        List<String> expectedList = List.of("key1", "val1", "key2", "val2");
        List<String> actualList = CommonUtil.mapToListOfString(map);
        assertLinesMatch(expectedList, actualList);
    }

    @Test
    void shouldReturnAnEmptyListWhenCallingMapToListAnEmptyMapIsPassed() {
        Map<String, String> map = Map.of();
        List<String> expectedList = List.of();
        List<String> actualList = CommonUtil.mapToListOfString(map);
        assertLinesMatch(expectedList, actualList);
    }

    @Test
    void shouldConvertListToMapOfStringWhenAListIsPassed() {
        List<String> list = List.of("key1", "val1", "key2", "val2");
        Map<String, String> expectedMap = new HashMap<>();
        expectedMap.put("key1", "val1");
        expectedMap.put("key2", "val2");
        Map<String, String> actualMap = CommonUtil.listToMapOfString(list);
        assertEquals(expectedMap, actualMap);
    }

}
