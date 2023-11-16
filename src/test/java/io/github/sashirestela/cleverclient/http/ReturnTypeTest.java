package io.github.sashirestela.cleverclient.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

class ReturnTypeTest {

    @Test
    void shouldReturnBaseClassForAMethod() throws NoSuchMethodException, SecurityException {
        var method = TestInterface.class.getMethod("asyncStreamMethod", new Class[] {});
        var returnType = new ReturnType(method);
        var actualClass = returnType.getBaseClass();
        var expectedClass = Object.class;
        assertEquals(expectedClass, actualClass);
    }

    @Test
    void shouldReturnCategoryAccordingToTheMethodType() throws NoSuchMethodException, SecurityException {
        var testData = Map.ofEntries(
                Map.entry("asyncStreamMethod", ReturnType.Category.ASYNC_STREAM),
                Map.entry("asyncListMethod", ReturnType.Category.ASYNC_LIST),
                Map.entry("asyncGenericMethod", ReturnType.Category.ASYNC_GENERIC),
                Map.entry("asyncObjectMethod", ReturnType.Category.ASYNC_OBJECT),
                Map.entry("asyncBinaryMethod", ReturnType.Category.ASYNC_BINARY),
                Map.entry("asyncStringMethod", ReturnType.Category.ASYNC_PLAIN_TEXT),
                Map.entry("syncStreamMethod", ReturnType.Category.SYNC_STREAM),
                Map.entry("syncListMethod", ReturnType.Category.SYNC_LIST),
                Map.entry("syncGenericMethod", ReturnType.Category.SYNC_GENERIC),
                Map.entry("syncObjectMethod", ReturnType.Category.SYNC_OBJECT),
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

    static interface TestInterface {

        CompletableFuture<Stream<Object>> asyncStreamMethod();

        CompletableFuture<List<Object>> asyncListMethod();

        CompletableFuture<Generic<Object>> asyncGenericMethod();

        CompletableFuture<Object> asyncObjectMethod();

        CompletableFuture<InputStream> asyncBinaryMethod();

        CompletableFuture<String> asyncStringMethod();

        CompletableFuture<Set<Object>> asyncSetMethod();

        Stream<Object> syncStreamMethod();

        List<Object> syncListMethod();

        Generic<Object> syncGenericMethod();

        Object syncObjectMethod();

        InputStream syncBinaryMethod();

        String syncStringMethod();

        Set<Object> syncSetMethod();
    }

    static interface Generic<T> {

    }
}