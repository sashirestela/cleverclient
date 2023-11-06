package io.github.sashirestela.cleverclient.util;

import java.util.*;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CommonUtilTest {

    @Test
    void shouldReturnTrueWhenObjectIsNullOrEmpty() {
        Object[] testData = {
                null,
                "",
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
    void shouldReturnTrueWhenListIsNullOrEmpty() {
        List<?>[] testData = { null, new ArrayList<>() };
        for (List<?> data : testData) {
            boolean actualCondition = CommonUtil.isNullOrEmpty(data);
            boolean expectedCondition = true;
            assertEquals(expectedCondition, actualCondition);
        }
    }

    @Test
    void shouldReturnFalseWhenListIsNotEmpty() {
        boolean actualCondition = CommonUtil.isNullOrEmpty(Arrays.asList("one", "two"));
        boolean expectedCondition = false;
        assertEquals(expectedCondition, actualCondition);
    }

    @Test
    void shouldReturnTrueWhenArrayIsNullOrEmpty() {
        Object[][] testData = { null, new String[] {} };
        for (Object[] data : testData) {
            boolean actualCondition = CommonUtil.isNullOrEmpty(data);
            boolean expectedCondition = true;
            assertEquals(expectedCondition, actualCondition);
        }
    }

    @Test
    void shouldReturnFalseWhenArrayIsNotEmpty() {
        boolean actualCondition = CommonUtil.isNullOrEmpty(new String[] { "one", "two" });
        boolean expectedCondition = false;
        assertEquals(expectedCondition, actualCondition);
    }

    @Test
    void shouldReturnTrueWhenStringIsNullOrEmptyOrBlank() {
        String[] testData = { null, "", " " };
        for (String data : testData) {
            boolean actualCondition = CommonUtil.isNullOrEmpty(data);
            boolean expectedCondition = true;
            assertEquals(expectedCondition, actualCondition);
        }
    }

    @Test
    void shouldReturnFalseWhenStringIsNotEmpty() {
        boolean actualCondition = CommonUtil.isNullOrEmpty("text");
        boolean expectedCondition = false;
        assertEquals(expectedCondition, actualCondition);
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
    void shouldDetectIfValueIsInTheRangeWhenTwoValuesAreCompared() {
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
}