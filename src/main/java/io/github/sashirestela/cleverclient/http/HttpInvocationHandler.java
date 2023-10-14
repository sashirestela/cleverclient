package io.github.sashirestela.cleverclient.http;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.sashirestela.cleverclient.CleverClient;

public class HttpInvocationHandler implements InvocationHandler {
  private static Logger logger = LoggerFactory.getLogger(HttpInvocationHandler.class);

  private CleverClient processor;
  private InvocationFilter filter;

  public HttpInvocationHandler(CleverClient processor, InvocationFilter filter) {
    this.processor = processor;
    this.filter = filter;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] arguments) throws Throwable {
    logger.debug("Invoked Method : {}.{}()", method.getDeclaringClass().getSimpleName(), method.getName());
    if (filter != null) {
      filter.apply(method, arguments);
      logger.debug("Applied Filter : {}", filter.getClass().getSimpleName());
    }
    var responseObject = processor.resolve(method, arguments);
    logger.debug("Received Response");

    return responseObject;
  }
}