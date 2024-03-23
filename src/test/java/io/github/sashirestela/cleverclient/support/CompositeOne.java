package io.github.sashirestela.cleverclient.support;

import io.github.sashirestela.cleverclient.annotation.StreamType;
import io.github.sashirestela.cleverclient.support.ReturnTypeTest.First;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@StreamType(type = First.class, events = { "first.create", "first.complete" })
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CompositeOne {
}
