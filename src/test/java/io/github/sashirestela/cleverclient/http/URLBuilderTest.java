package io.github.sashirestela.cleverclient.http;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.sashirestela.cleverclient.metadata.InterfaceMetadata.AnnotationMetadata;
import io.github.sashirestela.cleverclient.metadata.InterfaceMetadata.MethodMetadata;
import io.github.sashirestela.cleverclient.metadata.InterfaceMetadata.ParameterMetadata;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class URLBuilderTest {

    MethodMetadata methodMetadata = mock(MethodMetadata.class);
    URLBuilder urlBuilder = URLBuilder.one();

    @Test
    void shouldReturnUrlWithoutChangesWhenDoesNotContainPathOrQueryParams() {
        var url = "/api/domain/entities";

        when(methodMetadata.getPathParameters()).thenReturn(List.of());
        when(methodMetadata.getQueryParameters()).thenReturn(List.of());

        var actualUrl = urlBuilder.build(url, methodMetadata, null);
        var expectedUrl = url;
        assertEquals(expectedUrl, actualUrl);
    }

    @Test
    void shouldReturnReplacedUrlWithPathParamsWhenUrlContainsPathParams() {
        var url = "/api/domain/entities/{entityId}/details/{detailId}";
        var paramsList = List.of(
                ParameterMetadata.builder()
                        .index(1)
                        .annotation(AnnotationMetadata.builder()
                                .name("Path")
                                .isHttpMethod(false)
                                .valueByField(Map.of("value", "entityId"))
                                .build())
                        .build(),
                ParameterMetadata.builder()
                        .index(3)
                        .annotation(AnnotationMetadata.builder()
                                .name("Path")
                                .isHttpMethod(false)
                                .valueByField(Map.of("value", "detailId"))
                                .build())
                        .build());

        when(methodMetadata.getPathParameters()).thenReturn(paramsList);
        when(methodMetadata.getQueryParameters()).thenReturn(List.of());

        var actualUrl = urlBuilder.build(url, methodMetadata, new Object[] { null, 101, null, 201 });
        var expectedUrl = "/api/domain/entities/101/details/201";
        assertEquals(expectedUrl, actualUrl);
    }

    @Test
    void shouldReturnReplacedUrlWithQueryParamsWhenMethodContainsQueryParams() {
        var url = "/api/domain/entities";
        var paramsList = List.of(
                ParameterMetadata.builder()
                        .index(1)
                        .annotation(AnnotationMetadata.builder()
                                .name("Query")
                                .isHttpMethod(false)
                                .valueByField(Map.of("value", "sortedBy"))
                                .build())
                        .build(),
                ParameterMetadata.builder()
                        .index(2)
                        .annotation(AnnotationMetadata.builder()
                                .name("Query")
                                .isHttpMethod(false)
                                .valueByField(Map.of("value", "filterBy"))
                                .build())
                        .build(),
                ParameterMetadata.builder()
                        .index(3)
                        .annotation(AnnotationMetadata.builder()
                                .name("Query")
                                .isHttpMethod(false)
                                .valueByField(Map.of("value", "rowsPerPage"))
                                .build())
                        .build());

        when(methodMetadata.getPathParameters()).thenReturn(List.of());
        when(methodMetadata.getQueryParameters()).thenReturn(paramsList);

        var actualUrl = urlBuilder.build(url, methodMetadata, new Object[] { null, "name", null, 20 });
        var expectedUrl = "/api/domain/entities?sortedBy=name&rowsPerPage=20";
        assertEquals(expectedUrl, actualUrl);
    }

    @Test
    void shouldReturnReplacedUrlWithQueryParamsWhenMethodContainsQueryParamsForPojos() {
        var url = "/api/domain/entities";
        var paramsList = List.of(
                ParameterMetadata.builder()
                        .index(0)
                        .annotation(AnnotationMetadata.builder()
                                .name("Query")
                                .isHttpMethod(false)
                                .valueByField(Map.of("value", ""))
                                .build())
                        .build(),
                ParameterMetadata.builder()
                        .index(1)
                        .annotation(AnnotationMetadata.builder()
                                .name("Query")
                                .isHttpMethod(false)
                                .valueByField(Map.of("value", "sortedBy"))
                                .build())
                        .build());

        when(methodMetadata.getPathParameters()).thenReturn(List.of());
        when(methodMetadata.getQueryParameters()).thenReturn(paramsList);

        var actualUrl = urlBuilder.build(url, methodMetadata, new Object[] { new Pagination(10, 3), "fullname" });
        var expectedUrl = "/api/domain/entities?size=10&page=3&sortedBy=fullname";
        assertEquals(expectedUrl, actualUrl);
    }

    @Test
    void shouldReturnReplacedUrlWithQueryParamsWhenMethodContainsEnumQueryParams() {
        var url = "/api/domain/entities";
        var paramsList = List.of(
                ParameterMetadata.builder()
                        .index(0)
                        .annotation(AnnotationMetadata.builder()
                                .name("Query")
                                .isHttpMethod(false)
                                .valueByField(Map.of("value", "statusOpen"))
                                .build())
                        .build(),
                ParameterMetadata.builder()
                        .index(1)
                        .annotation(AnnotationMetadata.builder()
                                .name("Query")
                                .isHttpMethod(false)
                                .valueByField(Map.of("value", "statusClosed"))
                                .build())
                        .build());

        when(methodMetadata.getPathParameters()).thenReturn(List.of());
        when(methodMetadata.getQueryParameters()).thenReturn(paramsList);

        var actualUrl = urlBuilder.build(url, methodMetadata, new Object[] { Status.OPEN, Status.CLOSED });
        var expectedUrl = "/api/domain/entities?statusOpen=open&statusClosed=CLOSED";
        assertEquals(expectedUrl, actualUrl);
    }

    @Test
    void shouldReturnReplacedUrlWithQueryParamsWhenMethodContainsListQueryParams() {
        var url = "/api/domain/entities";
        var paramsList = List.of(
                ParameterMetadata.builder()
                        .index(0)
                        .annotation(AnnotationMetadata.builder()
                                .name("Query")
                                .isHttpMethod(false)
                                .valueByField(Map.of("value", "color"))
                                .build())
                        .build(),
                ParameterMetadata.builder()
                        .index(1)
                        .annotation(AnnotationMetadata.builder()
                                .name("Query")
                                .isHttpMethod(false)
                                .valueByField(Map.of("value", "size"))
                                .build())
                        .build());

        when(methodMetadata.getPathParameters()).thenReturn(List.of());
        when(methodMetadata.getQueryParameters()).thenReturn(paramsList);

        var actualUrl = urlBuilder.build(url, methodMetadata, new Object[] { List.of("red", "blue"), new String[] {"low", "high"} });
        var expectedUrl = "/api/domain/entities?color=red&color=blue&size=low&size=high";
        assertEquals(expectedUrl, actualUrl);
    }

    @Data
    @AllArgsConstructor
    static class Pagination {

        private Integer size;
        private Integer page;

    }

    enum Status {

        @JsonProperty("open")
        OPEN,

        CLOSED;
    }

}
