package io.github.sashirestela.cleverclient.metadata;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.sashirestela.cleverclient.annotation.Resource;
import io.github.sashirestela.cleverclient.http.ReturnType;
import io.github.sashirestela.cleverclient.util.Constant;

public class MetadataCollector {
    private static Logger logger = LoggerFactory.getLogger(MetadataCollector.class);

    private MetadataCollector() {
    }

    public static Metadata collect(Class<?> clazz) {
        var urlFromResource = Optional
                .ofNullable(getAnnotValue(clazz.getAnnotation(Resource.class)))
                .orElse("");
        Map<MethodSignature, Metadata.Method> methodsMap = new HashMap<>();
        var methods = clazz.getMethods();
        for (var method : methods) {
            var fullClassName = method.getGenericReturnType().getTypeName();
            var methodAnnotsList = getAnnotationsMetadata(method.getDeclaredAnnotations());
            var httpAnnotation = getAnnotIfIsInList(methodAnnotsList, Constant.HTTP_METHODS);
            var httpHeaders = getAnnotsIfIsInList(methodAnnotsList, Constant.HTTP_HEADERS);
            var isMultipart = (getAnnotIfIsInList(methodAnnotsList, Constant.MULTIPART_AS_LIST) != null);
            var urlFromHttp = httpAnnotation != null ? httpAnnotation.getValue() : "";
            var parametersByType = getParametersByType(method.getParameters());
            var methodMetadata = Metadata.Method.builder()
                    .name(method.getName())
                    .returnType(new ReturnType(fullClassName))
                    .isDefault(method.isDefault())
                    .httpAnnotation(httpAnnotation)
                    .httpHeaders(httpHeaders)
                    .isMultipart(isMultipart)
                    .url(urlFromResource + urlFromHttp)
                    .parametersByType(parametersByType)
                    .build();
            methodsMap.put(MethodSignature.of(method), methodMetadata);
        }
        var metadata = Metadata.builder()
                .name(clazz.getSimpleName())
                .methods(methodsMap)
                .build();
        logger.debug("Collected Metadata");
        return metadata;
    }

    private static Map<String, List<Metadata.Parameter>> getParametersByType(Parameter[] parameters) {
        Map<String, List<Metadata.Parameter>> parametersByType = new HashMap<>();
        for (var paramType : Constant.PARAMETER_TYPES) {
            parametersByType.put(paramType, new ArrayList<>());
        }
        var index = 0;
        for (var parameter : parameters) {
            var annotations = parameter.getDeclaredAnnotations();
            if (annotations.length > 0) {
                var annotation = annotations[0];
                var annotationValue = getAnnotValue(annotation);
                var paramMetadata = Metadata.Parameter.builder()
                        .index(index)
                        .type(parameter.getType())
                        .annotationValue(annotationValue)
                        .build();
                var annotationName = annotation.annotationType().getSimpleName();
                parametersByType.computeIfPresent(annotationName, (key, val) -> {
                    val.add(paramMetadata);
                    return val;
                });
            }
            index++;
        }
        return parametersByType;
    }

    private static List<Metadata.Annotation> getAnnotationsMetadata(Annotation[] annotations) {
        List<Metadata.Annotation> annotationsMetadata = new ArrayList<>();
        for (Annotation annotation : annotations) {
            String annotName = annotation.annotationType().getSimpleName();
            String annotValue = getAnnotValue(annotation);
            annotationsMetadata.add(new Metadata.Annotation(annotName, annotValue, annotation));
        }
        return annotationsMetadata;
    }

    private static String getAnnotValue(Annotation annotation) {
        if (annotation == null) {
            return null;
        }
        Object value;
        Class<? extends Annotation> annotType = annotation.annotationType();
        try {
            Method annotAttrib = annotType.getMethod(Constant.DEF_ANNOT_ATTRIB);
            value = annotAttrib.invoke(annotation, (Object[]) null);
        } catch (Exception e) {
            value = null;
        }
        return (String) value;
    }

    private static Metadata.Annotation getAnnotIfIsInList(
            List<Metadata.Annotation> annotations,
            List<String> annotationNames)
    {
        List<Metadata.Annotation> matchingAnnotations = getAnnotsIfIsInList(annotations, annotationNames);
        return matchingAnnotations.isEmpty() ? null : matchingAnnotations.get(0);
    }

    private static List<Metadata.Annotation> getAnnotsIfIsInList(
            List<Metadata.Annotation> annotations,
            List<String> annotationNames)
    {
        if (annotations.isEmpty()) {
            return List.of();
        } else {
            return annotations.stream()
                    .filter(annot -> annotationNames.contains(annot.getName()))
                    .collect(Collectors.toList());
        }
    }
}