package io.github.sashirestela.cleverclient.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.github.sashirestela.cleverclient.metadata.MethodSignature;
import lombok.AllArgsConstructor;
import lombok.Data;

import org.junit.jupiter.api.Test;

import io.github.sashirestela.cleverclient.metadata.Metadata;

class URLBuilderTest {

    Metadata metadata = mock(Metadata.class);
    URLBuilder urlBuilder = new URLBuilder(metadata);
    MethodSignature methodSign = MethodSignature.of("testMethod", List.of());

    @Test
    void shouldReturnUrlWithoutChangesWhenDoesNotContainPathOrQueryParams() {
        var methodName = "testMethod";
        var url = "/api/domain/entities";
        Map<String, List<Metadata.Parameter>> paramsMap = Map.of(
                "Path", new ArrayList<Metadata.Parameter>(),
                "Query", new ArrayList<Metadata.Parameter>());
        var methodMetadata = Metadata.Method.builder()
                .name(methodName)
                .url(url)
                .parametersByType(paramsMap)
                .build();
        var mapMethods = Map.of(methodSign, methodMetadata);

        when(metadata.getMethods()).thenReturn(mapMethods);

        var actualUrl = urlBuilder.build(methodSign, null);
        var expectedUrl = url;
        assertEquals(expectedUrl, actualUrl);
    }

    @Test
    void shouldReturnReplacedUrlWithPathParamsWhenUrlContainsPathParams() {
        var methodName = "testMethod";
        var url = "/api/domain/entities/{entityId}/details/{detailId}";
        var paramsList = List.of(
                Metadata.Parameter.builder()
                        .index(1)
                        .annotationValue("entityId")
                        .build(),
                Metadata.Parameter.builder()
                        .index(3)
                        .annotationValue("detailId")
                        .build());
        var paramsMap = Map.of(
                "Path", paramsList,
                "Query", new ArrayList<Metadata.Parameter>());
        var methodMetadata = Metadata.Method.builder()
                .name(methodName)
                .url(url)
                .parametersByType(paramsMap)
                .build();
        var mapMethods = Map.of(methodSign, methodMetadata);

        when(metadata.getMethods()).thenReturn(mapMethods);

        var actualUrl = urlBuilder.build(methodSign, new Object[] { null, 101, null, 201 });
        var expectedUrl = "/api/domain/entities/101/details/201";
        assertEquals(expectedUrl, actualUrl);
    }

    @Test
    void shouldReturnReplacedUrlWithQueryParamsWhenMethodContainsQueryParams() {
        var methodName = "testMethod";
        var url = "/api/domain/entities";
        var paramsList = List.of(
                Metadata.Parameter.builder()
                        .index(1)
                        .annotationValue("sortedBy")
                        .build(),
                Metadata.Parameter.builder()
                        .index(2)
                        .annotationValue("filterBy")
                        .build(),
                Metadata.Parameter.builder()
                        .index(3)
                        .annotationValue("rowsPerPage")
                        .build());
        var paramsMap = Map.of(
                "Path", new ArrayList<Metadata.Parameter>(),
                "Query", paramsList);
        var methodMetadata = Metadata.Method.builder()
                .name(methodName)
                .url(url)
                .parametersByType(paramsMap)
                .build();
        var mapMethods = Map.of(methodSign, methodMetadata);

        when(metadata.getMethods()).thenReturn(mapMethods);

        var actualUrl = urlBuilder.build(methodSign, new Object[] { null, "name", null, 20 });
        var expectedUrl = "/api/domain/entities?sortedBy=name&rowsPerPage=20";
        assertEquals(expectedUrl, actualUrl);
    }

    @Test
    void shouldReturnReplacedUrlWithQueryParamsWhenMethodContainsQueryParamsForPojos() {
        var methodName = "testMethod";
        var url = "/api/domain/entities";
        var paramsList = List.of(
                Metadata.Parameter.builder()
                        .index(0)
                        .annotationValue("")
                        .build(),
                Metadata.Parameter.builder()
                        .index(1)
                        .annotationValue("sortedBy")
                        .build());
        var paramsMap = Map.of(
                "Path", new ArrayList<Metadata.Parameter>(),
                "Query", paramsList);
        var methodMetadata = Metadata.Method.builder()
                .name(methodName)
                .url(url)
                .parametersByType(paramsMap)
                .build();
        var mapMethods = Map.of(methodSign, methodMetadata);

        when(metadata.getMethods()).thenReturn(mapMethods);

        var actualUrl = urlBuilder.build(methodSign, new Object[] { new Pagination(10, 3), "fullname" });
        var expectedUrl = "/api/domain/entities?size=10&page=3&sortedBy=fullname";
        assertEquals(expectedUrl, actualUrl);
    }

    @Data
    @AllArgsConstructor
    static class Pagination {
        private Integer size;
        private Integer page;
    }
}