package io.github.sashirestela.cleverclient.support;

import io.github.sashirestela.cleverclient.annotation.StreamType;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReturnTypeTest {

    @Test
    void shouldReturnBaseClassForAMethod() throws NoSuchMethodException, SecurityException {
        var method = TestInterface.class.getMethod("asyncStreamMethod", new Class[] {});
        var returnType = new ReturnType(method);
        var actualClass = returnType.getBaseClass();
        var expectedClass = MyClass.class;
        assertEquals(expectedClass, actualClass);
    }

    @Test
    void shouldReturnCategoryAccordingToTheMethodType() throws NoSuchMethodException, SecurityException {
        var testData = Map.ofEntries(
                Map.entry("asyncStreamObjectMethod", ReturnType.Category.ASYNC_STREAM_OBJECT),
                Map.entry("asyncStreamMethod", ReturnType.Category.ASYNC_STREAM),
                Map.entry("asyncListMethod", ReturnType.Category.ASYNC_LIST),
                Map.entry("asyncGenericMethod", ReturnType.Category.ASYNC_GENERIC),
                Map.entry("asyncMyClassMethod", ReturnType.Category.ASYNC_CUSTOM),
                Map.entry("asyncBinaryMethod", ReturnType.Category.ASYNC_BINARY),
                Map.entry("asyncStringMethod", ReturnType.Category.ASYNC_PLAIN_TEXT),
                Map.entry("syncStreamObjectMethod", ReturnType.Category.SYNC_STREAM_OBJECT),
                Map.entry("syncStreamMethod", ReturnType.Category.SYNC_STREAM),
                Map.entry("syncListMethod", ReturnType.Category.SYNC_LIST),
                Map.entry("syncGenericMethod", ReturnType.Category.SYNC_GENERIC),
                Map.entry("syncMyClassMethod", ReturnType.Category.SYNC_CUSTOM),
                Map.entry("syncBinaryMethod", ReturnType.Category.SYNC_BINARY),
                Map.entry("syncStringMethod", ReturnType.Category.SYNC_PLAIN_TEXT));
        for (String methodName : testData.keySet()) {
            var method = TestInterface.class.getMethod(methodName, new Class[] {});
            var returnType = new ReturnType(method);
            var actualCategory = returnType.category();
            var expectedCategory = testData.get(methodName);
            assertEquals(expectedCategory, actualCategory);
        }
    }

    @Test
    void shouldReturnNullCategoryWhenMethodReturnTypeIsNotExpected() throws NoSuchMethodException, SecurityException {
        var method = TestInterface.class.getMethod("asyncSetMethod", new Class[] {});
        var returnType = new ReturnType(method);
        assertNull(returnType.category());
        method = TestInterface.class.getMethod("syncSetMethod", new Class[] {});
        returnType = new ReturnType(method);
        assertNull(returnType.category());
    }

    @Test
    void shouldReturnMapClassByEventWhenTheMethodIsAnnotatedWithStreamType()
            throws NoSuchMethodException, SecurityException {
        var method = TestInterface.class.getMethod("asyncStreamObjectMethod", new Class[] {});
        var returnType = new ReturnType(method);
        var actualMap = returnType.getClassByEvent();
        var expectedMap = new ConcurrentHashMap<>();
        expectedMap.put(CleverClientSSE.EVENT_HEADER + "first.create", First.class);
        expectedMap.put(CleverClientSSE.EVENT_HEADER + "first.complete", First.class);
        expectedMap.put(CleverClientSSE.EVENT_HEADER + "second.create", Second.class);
        assertTrue(expectedMap.equals(actualMap));
    }

    static interface TestInterface {

        @StreamType(type = First.class, events = { "first.create", "first.complete" })
        @StreamType(type = Second.class, events = { "second.create" })
        CompletableFuture<Stream<Object>> asyncStreamObjectMethod();

        CompletableFuture<Stream<MyClass>> asyncStreamMethod();

        CompletableFuture<List<MyClass>> asyncListMethod();

        CompletableFuture<Generic<MyClass>> asyncGenericMethod();

        CompletableFuture<MyClass> asyncMyClassMethod();

        CompletableFuture<InputStream> asyncBinaryMethod();

        CompletableFuture<String> asyncStringMethod();

        CompletableFuture<Set<MyClass>> asyncSetMethod();

        Stream<Object> syncStreamObjectMethod();

        Stream<MyClass> syncStreamMethod();

        List<MyClass> syncListMethod();

        Generic<MyClass> syncGenericMethod();

        MyClass syncMyClassMethod();

        InputStream syncBinaryMethod();

        String syncStringMethod();

        Set<MyClass> syncSetMethod();

    }

    static class First {
    }

    static class Second {
    }

    static class MyClass {
    }

    static interface Generic<T> {
    }

}
