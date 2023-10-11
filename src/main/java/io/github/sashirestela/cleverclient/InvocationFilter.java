package io.github.sashirestela.cleverclient;

import java.lang.reflect.Method;

public interface InvocationFilter {

  void apply(Method method, Object[] arguments);

}