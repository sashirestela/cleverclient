package io.github.sashirestela.cleverclient.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ReflectUtil {
    private ReflectUtil() {
    }

    @SuppressWarnings("unchecked")
    public static <T> T createProxy(Class<T> interfaceClass, InvocationHandler handler) {
        return (T) Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class<?>[] { interfaceClass },
                handler);
    }

    public static Map<String, Object> getMapFields(List<Object> objects) {
        Map<String, Object> structure = new HashMap<>();
        for (Object object : objects) {
            structure.putAll(getMapFields(object));
        }

        return structure;
    }

    public static Map<String, Object> getMapFields(Object object) {
        if (object instanceof Map) {
            return JsonUtil.objectToMap(object);
        }

        final var GET_PREFIX = "get";
        Map<String, Object> structure = new HashMap<>();
        var clazz = object.getClass();
        var fields = getFields(clazz);
        for (var field : fields) {
            var fieldName = field.getName();
            var methodName = GET_PREFIX + CommonUtil.capitalize(fieldName);
            Object fieldValue = null;
            try {
                var getMethod = clazz.getMethod(methodName);
                fieldValue = getMethod.invoke(object);
            } catch (Exception e) {
                // This shouldn't happen
            }
            if (fieldValue != null) {
                structure.put(getFieldName(field), getFieldValue(fieldValue));
            }
        }
        return structure;
    }

    private static Field[] getFields(Class<?> clazz) {
        final var CLASS_OBJECT = "Object";
        var fields = new Field[] {};
        var nextClazz = clazz;
        while (!nextClazz.getSimpleName().equals(CLASS_OBJECT)) {
            fields = CommonUtil.concatArrays(fields, nextClazz.getDeclaredFields());
            nextClazz = nextClazz.getSuperclass();
        }
        return fields;
    }

    private static String getFieldName(Field field) {
        final var JSON_PROPERTY_METHOD_NAME = "value";
        var fieldName = field.getName();
        if (field.isAnnotationPresent(JsonProperty.class)) {
            fieldName = (String) getAnnotAttribValue(field, JsonProperty.class, JSON_PROPERTY_METHOD_NAME);
        }
        return fieldName;
    }

    private static Object getFieldValue(Object fieldValue) {
        if (fieldValue.getClass().isEnum()) {
            var enumConstantName = ((Enum<?>) fieldValue).name();
            try {
                fieldValue = fieldValue.getClass().getField(enumConstantName).getAnnotation(JsonProperty.class).value();
            } catch (NoSuchFieldException | SecurityException e) {
                // This shouldn't happen
            }
        }
        return fieldValue;
    }

    private static Object getAnnotAttribValue(AnnotatedElement element, Class<? extends Annotation> annotType,
            String annotAttribName) {
        Object value = null;
        var annotation = element.getAnnotation(annotType);
        if (annotation != null) {
            Method annotAttrib = null;
            try {
                annotAttrib = annotType.getMethod(annotAttribName);
                value = annotAttrib.invoke(annotation, (Object[]) null);
            } catch (Exception e) {
                // This shouldn't happen
            }
        }
        return value;
    }
}