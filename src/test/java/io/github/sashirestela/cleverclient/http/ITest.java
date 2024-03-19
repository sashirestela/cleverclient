package io.github.sashirestela.cleverclient.http;

import io.github.sashirestela.cleverclient.annotation.Body;
import io.github.sashirestela.cleverclient.annotation.GET;
import io.github.sashirestela.cleverclient.annotation.Header;
import io.github.sashirestela.cleverclient.annotation.Multipart;
import io.github.sashirestela.cleverclient.annotation.POST;
import io.github.sashirestela.cleverclient.annotation.Path;
import io.github.sashirestela.cleverclient.annotation.Query;
import io.github.sashirestela.cleverclient.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public interface ITest {

    interface NotAnnotatedService {

        @GET("/demos")
        CompletableFuture<List<Demo>> goodMethod();

        String unannotatedMethod();

    }

    interface BadPathParamService {

        @GET("/demos")
        CompletableFuture<List<Demo>> goodMethod();

        @POST("/demos/{demoId}")
        CompletableFuture<Demo> unmatchedPathParamMethod(@Path("demo_id") int demoId);

    }

    @Resource("/api")
    @Header(name = "FirstKey", value = "FirstVal")
    @Header(name = "SecondKey", value = "SecondVal")
    interface GoodService {

        @POST("/demos/{demoId}")
        @Multipart
        @Header(name = "ThirdKey", value = "ThirdVal")
        Demo demoPostMethod(@Body RequestDemo request, @Path("demoId") Long demoId);

        @GET("/demos/{demoId}/subdemos")
        List<Demo> demoGetMethod(@Path("demoId") Long demoId, @Query("size") Integer size, @Query("page") Integer page);

    }

    @Resource("/demos")
    interface AsyncService {

        @GET
        Set<Demo> unsupportedMethod();

        @GET("/{demoId}")
        CompletableFuture<String> getDemoPlain(@Path("demoId") Integer demoId);

        @GET("/{demoId}")
        CompletableFuture<InputStream> getDemoBinary(@Path("demoId") Integer demoId);

        @GET("/{demoId}")
        CompletableFuture<Demo> getDemo(@Path("demoId") Integer demoId);

        @GET("/{genericDemoId}")
        CompletableFuture<Generic<Demo>> getGenericDemo(@Path("genericDemoId") Integer genericDemoId);

        @GET
        @Header(name = "MyHeader", value = "MyValue")
        @Header(name = "OtherHeader", value = "OtherValue")
        CompletableFuture<List<Demo>> getDemos();

        @POST
        CompletableFuture<Stream<Demo>> getDemoStream(@Body RequestDemo request);

        @Multipart
        @POST
        CompletableFuture<Demo> getFile(@Body RequestDemo request);

        default String defaultMethod(String name) {
            return "Hello " + name;
        }

    }

    @Resource("/demos")
    interface SyncService {

        @GET("/{demoId}")
        String getDemoPlain(@Path("demoId") Integer demoId);

        @GET("/{demoId}")
        InputStream getDemoBinary(@Path("demoId") Integer demoId);

        @GET("/{demoId}")
        Demo getDemo(@Path("demoId") Integer demoId);

        @GET("/{genericDemoId}")
        Generic<Demo> getGenericDemo(@Path("genericDemoId") Integer genericDemoId);

        @GET
        @Header(name = "MyHeader", value = "MyValue")
        List<Demo> getDemos();

        @POST
        Stream<Demo> getDemoStream(@Body RequestDemo request);

    }

    interface NotSavedService {

        @GET("/demos")
        CompletableFuture<List<Demo>> goodMethod();

    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @ToString
    @EqualsAndHashCode
    static class Demo {

        private Integer id;

        private String description;

        private Boolean active;

    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    static class RequestDemo {

        private String prefix;

        private java.nio.file.Path file;

    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    static class Generic<T> {

        private Integer id;

        private List<T> listDemo;

    }

}
