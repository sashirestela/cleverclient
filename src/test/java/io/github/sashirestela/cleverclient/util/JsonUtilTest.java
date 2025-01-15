package io.github.sashirestela.cleverclient.util;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.sashirestela.cleverclient.support.CleverClientException;
import io.github.sashirestela.cleverclient.test.TestSupport;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JsonUtilTest {

    @BeforeAll
    static void setup() {
        TestSupport.setupConfigurator();
    }

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
        String json = "{\"first\":\"test\",\"second\":\"WRONG TYPE\"}";
        assertThrows(CleverClientException.class, () -> JsonUtil.jsonToObject(json, TestClass.class));
    }

    @Test
    void shouldGracefullyIgnoreUnknownPropertiesWhenConvertingJsonToObject() {
        String json = "{\"first\":\"test\",\"unknown_property\":1}";
        TestClass actualObject = JsonUtil.jsonToObject(json, TestClass.class);
        TestClass expectedObject = new TestClass("test", null);
        assertEquals(expectedObject.getFirst(), actualObject.getFirst());
        assertEquals(expectedObject.getSecond(), actualObject.getSecond());
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
        String json = "[{\"first\":\"test1\",\"second\":10},{\"first\":[\"WRONG TYPE\"],\"second\":\"WRONG TYPE\"}]";
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
                "{\"first\":\"test2\",\"second\":\"WRONG TYPE\"}]}";
        assertThrows(CleverClientException.class,
                () -> JsonUtil.jsonToParametricObject(json, TestGeneric.class, TestClass.class));
    }

    @Test
    void shouldConvertObjectToMapWhenClassHasNoIssues() {
        ChildTestClass object = ChildTestClass.builder()
                .integerField(10)
                .stringField("text")
                .doubleField(3.1416)
                .property(20)
                .testEnumField(TestEnum.ENUM1)
                .build();
        Map<String, Object> actualMapFields = JsonUtil.objectToMap(object);
        Map<String, Object> expectedMapFields = Map.of(
                "integer", 10,
                "string", "text",
                "real", 3.1416,
                "property", 20,
                "enumerator", "enum1");
        assertEquals(expectedMapFields, actualMapFields);
    }

    @Test
    void shouldThrowExceptionWhenConvertingObjectToMapWithIssues() {
        FailClass object = new FailClass("test", 10);
        assertThrows(CleverClientException.class, () -> JsonUtil.objectToMap(object));
    }

    @Test
    void shouldHandleLocalDateWhenAnObjectMapperWithTimeModuleIsPassed() {
        String expectedJson = "{\"date\":[2024,12,31]}";
        String actualJson = JsonUtil.objectToJson(new DateClass(LocalDate.of(2024, 12, 31)));
        assertEquals(expectedJson, actualJson);
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

    static enum TestEnum {
        @JsonProperty("enum1")
        ENUM1,
        @JsonProperty("enum2")
        ENUM2;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @SuperBuilder
    @Getter
    static class SuperTestClass {

        @JsonProperty("integer")
        protected Integer integerField;
        @JsonProperty("string")
        protected String stringField;
        @JsonProperty("real")
        protected Double doubleField;

    }

    @NoArgsConstructor
    @AllArgsConstructor
    @SuperBuilder
    @Getter
    @Setter
    static class ChildTestClass extends SuperTestClass {

        private Integer property;
        @JsonProperty("enumerator")
        private TestEnum testEnumField;

    }

    @NoArgsConstructor
    @AllArgsConstructor
    @SuppressWarnings("unused")
    static class FailClass {

        private String first;

        private Integer second;

    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    static class DateClass {

        private LocalDate date;

    }

}
