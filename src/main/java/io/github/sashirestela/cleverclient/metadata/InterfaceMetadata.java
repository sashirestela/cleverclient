package io.github.sashirestela.cleverclient.metadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.github.sashirestela.cleverclient.http.ReturnType;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class InterfaceMetadata {
    private final static String ANNOT_RESOURCE = "Resource";
    private final static String ANNOT_HEADER = "Header";
    private final static String ANNOT_MULTIPART = "Multipart";
    private final static String ANNOT_PARAM_BODY = "Body";
    private final static String ANNOT_PARAM_PATH = "Path";
    private final static String ANNOT_PARAM_QUERY = "Query";
    private final static String ANNOT_FIELD_NAME = "name";
    private final static String ANNOT_FIELD_VALUE = "value";

    String name;
    List<AnnotationMetadata> annotations;
    Map<String, MethodMetadata> methodBySignature;

    public String getFullUrlByMethod(MethodMetadata methodMetadata) {
        var resourceAnnot = annotations.stream()
                .filter(annot -> annot.getName().equals(ANNOT_RESOURCE))
                .findFirst();
        var resourceUrl = resourceAnnot.isPresent() ? resourceAnnot.get().getValue() : "";
        var httpMethodAnnot = methodMetadata.getAnnotations().stream()
                .filter(AnnotationMetadata::isHttpMethod)
                .findFirst();
        var httpMethodUrl = httpMethodAnnot.isPresent() ? httpMethodAnnot.get().getValue() : "";
        return resourceUrl + httpMethodUrl;
    }

    public List<String> getFullHeadersByMethod(MethodMetadata methodMetadata) {
        var fullHeaderAnnots = annotations.stream()
                .filter(annot -> annot.getName().equals(ANNOT_HEADER))
                .collect(Collectors.toList());
        var methodHeaderAnnots = methodMetadata.getAnnotations().stream()
                .filter(annot -> annot.getName().equals(ANNOT_HEADER))
                .collect(Collectors.toList());
        fullHeaderAnnots.addAll(methodHeaderAnnots);
        List<String> fullHeaders = new ArrayList<>();
        for (AnnotationMetadata annot : fullHeaderAnnots) {
            fullHeaders.add(annot.getValueByField().get(ANNOT_FIELD_NAME));
            fullHeaders.add(annot.getValueByField().get(ANNOT_FIELD_VALUE));
        }
        return fullHeaders;
    }

    @Value
    @Builder
    public static class MethodMetadata {
        String name;
        ReturnType returnType;
        boolean isDefault;
        List<AnnotationMetadata> annotations;
        List<ParameterMetadata> parameters;

        public boolean hasHttpAnnotation() {
            return annotations.stream()
                    .anyMatch(AnnotationMetadata::isHttpMethod);
        }

        public String getHttpAnnotationName() {
            return annotations.stream()
                    .filter(AnnotationMetadata::isHttpMethod)
                    .findFirst()
                    .get()
                    .getName();
        }

        public boolean isMultipart() {
            return annotations.stream()
                    .anyMatch(annot -> annot.getName().equals(ANNOT_MULTIPART));
        }

        public int getBodyIndex() {
            var bodyParam = parameters.stream()
                    .filter(param -> param.getAnnotation().getName().equals(ANNOT_PARAM_BODY))
                    .findFirst();
            return bodyParam.isPresent() ? bodyParam.get().getIndex() : -1;
        }

        public Map<Integer, String> getPathParameters() {
            return getParametersFilteredBy(ANNOT_PARAM_PATH);
        }

        public Map<Integer, String> getQueryParameters() {
            return getParametersFilteredBy(ANNOT_PARAM_QUERY);
        }

        private Map<Integer, String> getParametersFilteredBy(String annotationName) {
            var filteredParameters = parameters.stream()
                    .filter(param -> param.getAnnotation() != null)
                    .filter(param -> param.getAnnotation().getName().equals(annotationName))
                    .collect(Collectors.toList());
            return filteredParameters.stream().count() > 0
                    ? filteredParameters.stream()
                            .collect(Collectors.toMap(p -> p.getIndex(), p -> p.getAnnotation().getValue()))
                    : new HashMap<>();
        }
    }

    @Value
    @Builder
    public static class ParameterMetadata {
        int index;
        Class<?> type;
        AnnotationMetadata annotation;
    }

    @Value
    @Builder
    public static class AnnotationMetadata {
        String name;
        boolean isHttpMethod;
        Map<String, String> valueByField;

        public String getValue() {
            return valueByField.get(ANNOT_FIELD_VALUE);
        }
    }
}