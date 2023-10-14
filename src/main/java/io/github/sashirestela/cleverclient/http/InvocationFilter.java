package io.github.sashirestela.cleverclient.http;

import java.lang.reflect.Method;

public interface InvocationFilter {

  void apply(Method method, Object[] arguments);

}