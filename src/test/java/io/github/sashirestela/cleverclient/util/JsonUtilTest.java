package io.github.sashirestela.cleverclient.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.github.sashirestela.cleverclient.support.CleverClientException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

class JsonUtilTest {

    @Test
    void shouldConvertObjectToJsonWhenClassHasNoIssues() {
        TestClass object = new TestClass("test", 10);
        String actualJson = JsonUtil.objectToJson(object);
        String expectedJson = "{\"first\":\"test\",\"second\":10}";
        assertEquals(expectedJson, actualJson);
    }

    @Test
    void shouldThrowExceptionWhenConvertingAnObjectOfClassWithIssues() {
        FailClass object = new FailClass("test", 10);
        assertThrows(CleverClientException.class, () -> JsonUtil.objectToJson(object));
    }

    @Test
    void shouldConvertJsonToObjectWhenJsonHasNoIssues() {
        String json = "{\"first\":\"test\",\"second\":10}";
        TestClass actualObject = JsonUtil.jsonToObject(json, TestClass.class);
        TestClass expectedObject = new TestClass("test", 10);
        assertEquals(expectedObject.getFirst(), actualObject.getFirst());
        assertEquals(expectedObject.getSecond(), actualObject.getSecond());
    }

    @Test
    void shouldThrowExceptionWhenConvertingJsonToObjectWithIssues() {
        String json = "{\"first\":\"test\",\"secondish\":10}";
        assertThrows(CleverClientException.class, () -> JsonUtil.jsonToObject(json, TestClass.class));
    }

    @Test
    void shouldConvertJsonToListWhenJsonHasNoIssues() {
        String json = "[{\"first\":\"test1\",\"second\":10},{\"first\":\"test2\",\"second\":20}]";
        List<TestClass> actualList = JsonUtil.jsonToList(json, TestClass.class);
        List<TestClass> expectedList = Arrays.asList(
                new TestClass("test1", 10),
                new TestClass("test2", 20));
        assertEquals(expectedList.size(), actualList.size());
        assertEquals(expectedList.get(0).getFirst(), actualList.get(0).getFirst());
        assertEquals(expectedList.get(0).getSecond(), actualList.get(0).getSecond());
        assertEquals(expectedList.get(1).getFirst(), actualList.get(1).getFirst());
        assertEquals(expectedList.get(1).getSecond(), actualList.get(1).getSecond());
    }

    @Test
    void shouldThrowExceptionWhenConvertingJsonToListWithIssues() {
        String json = "[{\"first\":\"test1\",\"second\":10},{\"firstish\":\"test2\",\"secondish\":20}]";
        assertThrows(CleverClientException.class, () -> JsonUtil.jsonToList(json, TestClass.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldConvertJsonToParametricObjectWhenJsonHasNoIssues() {
        String json = "{\"id\":\"abc\",\"data\":[{\"first\":\"test1\",\"second\":10}," +
                "{\"first\":\"test2\",\"second\":20}]}";
        TestGeneric<TestClass> actualObject = JsonUtil.jsonToParametricObject(json, TestGeneric.class,
                TestClass.class);
        List<TestClass> actualList = actualObject.getData();
        List<TestClass> expectedList = Arrays.asList(
                new TestClass("test1", 10),
                new TestClass("test2", 20));
        TestGeneric<TestClass> expectedObject = new TestGeneric<>("abc", expectedList);

        assertEquals(expectedObject.getId(), actualObject.getId());
        assertEquals(expectedList.size(), actualList.size());
        assertEquals(expectedList.get(0).getFirst(), actualList.get(0).getFirst());
        assertEquals(expectedList.get(0).getSecond(), actualList.get(0).getSecond());
        assertEquals(expectedList.get(1).getFirst(), actualList.get(1).getFirst());
        assertEquals(expectedList.get(1).getSecond(), actualList.get(1).getSecond());
    }

    @Test
    void shouldThrowExceptionWhenConvertingJsonToParametricObjectWithIssues() {
        String json = "{\"id\":\"abc\",\"data\":[{\"first\":\"test1\",\"second\":10}," +
                "{\"firstish\":\"test2\",\"secondish\":20}]}";
        assertThrows(CleverClientException.class,
                () -> JsonUtil.jsonToParametricObject(json, TestGeneric.class, TestClass.class));
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    static class TestClass {

        @JsonProperty(required = true)
        public String first;

        public Integer second;

    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    static class TestGeneric<T> {

        private String id;

        private List<T> data;

    }

    @NoArgsConstructor
    @AllArgsConstructor
    @SuppressWarnings("unused")
    static class FailClass {

        private String first;

        private Integer second;

    }

}