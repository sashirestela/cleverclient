package io.github.sashirestela.cleverclient.support;

import io.github.sashirestela.cleverclient.Event;
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
                Map.entry("asyncStreamEventMethod", ReturnType.Category.ASYNC_STREAM_EVENT),
                Map.entry("asyncStreamMethod", ReturnType.Category.ASYNC_STREAM),
                Map.entry("asyncListMethod", ReturnType.Category.ASYNC_LIST),
                Map.entry("asyncGenericMethod", ReturnType.Category.ASYNC_GENERIC),
                Map.entry("asyncMyClassMethod", ReturnType.Category.ASYNC_CUSTOM),
                Map.entry("asyncBinaryMethod", ReturnType.Category.ASYNC_BINARY),
                Map.entry("asyncStringMethod", ReturnType.Category.ASYNC_PLAIN_TEXT),
                Map.entry("syncStreamEventMethod", ReturnType.Category.SYNC_STREAM_EVENT),
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
    void shouldReturnMapClassByEventWhenTheMethodIsAnnotatedWithCompositeMultiStreamType()
            throws NoSuchMethodException, SecurityException {
        var method = TestInterface.class.getMethod("asyncStreamEventMethod", new Class[] {});
        var returnType = new ReturnType(method);
        var actualMap = returnType.getClassByEvent();
        var expectedMap = new ConcurrentHashMap<>();
        expectedMap.put("first.create", First.class);
        expectedMap.put("first.complete", First.class);
        expectedMap.put("second.create", Second.class);
        assertEquals(Boolean.TRUE, expectedMap.equals(actualMap));
    }

    @Test
    void shouldReturnMapClassByEventWhenTheMethodIsAnnotatedWithCompositeSingleStreamType()
            throws NoSuchMethodException, SecurityException {
        var method = TestInterface.class.getMethod("syncStreamEventMethod", new Class[] {});
        var returnType = new ReturnType(method);
        var actualMap = returnType.getClassByEvent();
        var expectedMap = new ConcurrentHashMap<>();
        expectedMap.put("first.create", First.class);
        expectedMap.put("first.complete", First.class);
        assertEquals(Boolean.TRUE, expectedMap.equals(actualMap));
    }

    static interface TestInterface {

        @CompositeTwo
        CompletableFuture<Stream<Event>> asyncStreamEventMethod();

        CompletableFuture<Stream<MyClass>> asyncStreamMethod();

        CompletableFuture<List<MyClass>> asyncListMethod();

        CompletableFuture<Generic<MyClass>> asyncGenericMethod();

        CompletableFuture<MyClass> asyncMyClassMethod();

        CompletableFuture<InputStream> asyncBinaryMethod();

        CompletableFuture<String> asyncStringMethod();

        CompletableFuture<Set<MyClass>> asyncSetMethod();

        @CompositeOne
        Stream<Event> syncStreamEventMethod();

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
