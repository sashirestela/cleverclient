package io.github.sashirestela.cleverclient.http;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import io.github.sashirestela.cleverclient.metadata.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpInvocationHandler implements InvocationHandler {
    private static final Logger logger = LoggerFactory.getLogger(HttpInvocationHandler.class);

    private final HttpProcessor processor;
    private final Metadata metadata;

    public HttpInvocationHandler(HttpProcessor processor, Metadata metadata) {
        this.processor = processor;
        this.metadata = metadata;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] arguments) throws Throwable {
        logger.debug("Invoked Method : {}.{}()", method.getDeclaringClass().getSimpleName(), method.getName());
        if (method.isDefault()) {
            return MethodHandles.lookup()
                    .findSpecial(
                            method.getDeclaringClass(),
                            method.getName(),
                            MethodType.methodType(
                                    method.getReturnType(),
                                    method.getParameterTypes()),
                            method.getDeclaringClass())
                    .bindTo(proxy)
                    .invokeWithArguments(arguments);
        } else {
            var responseObject = processor.resolve(metadata, method, arguments);
            logger.debug("Received Response");
            return responseObject;
        }
    }
}