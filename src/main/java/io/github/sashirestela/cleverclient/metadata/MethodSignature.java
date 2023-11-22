package io.github.sashirestela.cleverclient.metadata;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class MethodSignature {

    private final String name;
    private final List<Type> parameterTypes;


    private MethodSignature(String name, List<Type> parameterTypes) {
        this.name = name;
        this.parameterTypes = parameterTypes;
    }

    public static MethodSignature of(String methodName, List<Type> parameterTypes) {
        return new MethodSignature(methodName, new ArrayList<>(parameterTypes));
    }

    public static MethodSignature of(Method method) {
        Objects.requireNonNull(method, "method");
        return new MethodSignature(
                method.getName(),
                Arrays.asList(method.getGenericParameterTypes())
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MethodSignature)) return false;
        MethodSignature that = (MethodSignature) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(parameterTypes, that.parameterTypes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, parameterTypes);
    }
}
