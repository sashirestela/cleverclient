package io.github.sashirestela.cleverclient.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import io.github.sashirestela.cleverclient.annotation.GET;
import io.github.sashirestela.cleverclient.annotation.Path;

class ReflectUtilTest {

    @Test
    void shouldExecuteHandlerWhenInterfaceMethodIsCalled() {
        TestInterface test = ReflectUtil.createProxy(TestInterface.class, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                return "Text from handler.";
            }
        });
        String actualValue = test.testMethod("example");
        String expectedValue = "Text from handler.";
        assertEquals(expectedValue, actualValue);
    }

    static interface TestInterface {
        @GET("/api/test/url")
        String testMethod(@Path("arg") String argument);
    }
}