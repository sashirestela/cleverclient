package io.github.sashirestela.cleverclient.annotation;

import io.github.sashirestela.cleverclient.annotation.StreamType.List;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Repeatable(List.class)
public @interface StreamType {

    Class<?> type();

    String[] events();

    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface List {

        StreamType[] value();

    }

}
