# 💎 CleverClient

A Java library for making http client and websocket requests easily.

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=sashirestela_cleverclient&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=sashirestela_cleverclient)
[![codecov](https://codecov.io/gh/sashirestela/cleverclient/graph/badge.svg?token=PEYAFW3EWD)](https://codecov.io/gh/sashirestela/cleverclient)
![Maven Central](https://img.shields.io/maven-central/v/io.github.sashirestela/cleverclient)
![GitHub Workflow Status](https://img.shields.io/github/actions/workflow/status/sashirestela/cleverclient/build_java_maven.yml)

### Table of Contents
- [Description](#-description)
- [How to Use](#-how-to-use)
- [Installation](#-installation)
- [Features](#-features)
  - [CleverClient Creation](#cleverclient-creation)
  - [Interface Annotations](#interface-annotations)
  - [Supported Response Types](#supported-response-types)
  - [Interface Default Methods](#interface-default-methods)
  - [Exception Handling](#exception-handling)
  - [WebSocket](#websocket)
- [Examples](#-examples)
- [Contributing](#-contributing)
- [License](#-license)
- [Show Us Your Love](#-show-us-your-love)

## 💡 Description

CleverClient is a Java library that simplifies requesting client-side Http services and websockets using annotated interfaces and methods. CleverClient uses behind the scenes two out-of-box Http client libraries: [Java's HttpClient](https://docs.oracle.com/en/java/javase/11/docs/api/java.net.http/java/net/http/HttpClient.html) (by default) or [Square's OkHttp](https://square.github.io/okhttp/) (adding a dependency).

## 📘 How to Use

For example, if we want to use the public API [JsonPlaceHolder](https://jsonplaceholder.typicode.com/) and call its endpoint ```/posts```, we just have to create an entity ```Post```, an interface ```PostService``` with special annotatons, and call the API through ```CleverClient```:

```java
// Entity
public class Post {
  private Integer id;
  private String title;
  private String body;
  private Integer userId;

  // Constructors , getters, setters, etc.
}

// Interface
@Resource("/posts")
public interface PostService {

  @GET
  List<Post> readPosts(@Query("_page") Integer page, @Query("_limit") Integer limit);

  @GET("/{postId}")
  Post readPost(@Path("postId") Integer postId);

  @POST
  Post createPost(@Body Post post);

}

// Use CleverClient to call the API
var cleverClient = CleverClient.builder()
    .baseUrl("https://jsonplaceholder.typicode.com")
    .build();

var postService = cleverClient.create(PostService.class);

var page = 1;
var limit = 5;
var postId = 17;
var userId = 3;

// Example Read Posts
var postsList = postService.readPosts(page, limit);
postsList.forEach(System.out::println);

// Example Read Post
var onePost = postService.readPost(postId);
System.out.println(onePost);

// Example Create Post
var newPost = postService.createPost(new Post(
    null,
    "Hello",
    "Hello word, you are very welcome!",
    userId));
System.out.println(newPost);
```

## ⚙ Installation

You can install CleverClient by adding the following dependencies to your Maven project:
```xml
<dependency>
    <groupId>io.github.sashirestela</groupId>
    <artifactId>cleverclient</artifactId>
    <version>[cleverclient_latest_version]</version>
</dependency>
<!-- OkHttp dependency is optional if you decide to use it with CleverClient -->
<dependency>
    <groupId>com.squareup.okhttp3</groupId>
    <artifactId>okhttp</artifactId>
    <version>[okhttp_latest_version]</version>
</dependency>
```

Or alternatively using Gradle:

```groovy
dependencies {
    implementation("io.github.sashirestela:cleverclient:[cleverclient_latest_version]")
    /* OkHttp dependency is optional if you decide to use it with CleverClient */
    implementation("com.squareup.okhttp3:okhttp:[okhttp_latest_version]")
}
```

Take in account that you need to use **Java 11 or greater**.

## 📕 Features

### CleverClient Creation

We have the following attributes to create a CleverClient object:

| Attribute          | Description                                                  | Required  |
| -------------------|--------------------------------------------------------------|-----------|
| baseUrl            | Api's url                                                    | mandatory |
| headers            | Map of headers (name/value)                                  | optional  |
| header             | Single header as a name and a value                          | optional  |
| bodyInspector      | Function to inspect the `@Body` request parameter            | optional  |
| requestInterceptor | Function to modify the request once is built                 | optional  |
| responseInterceptor| Function to modify the response after it's received          | optional  |
| clientAdapter      | Http client implementation (Java HttpClient or OkHttp based) | optional  |
| endsOfStream       | List of texts used to mark the end of streams                | optional  |
| endOfStream        | Text used to mark the end of streams                         | optional  |
| objectMapper       | Provides Json conversions either to/from objects             | optional  |

```end(s)OfStream``` is required when you have endpoints sending back streams of data (Server Sent Events - SSE).

The attribute ```clientAdapter``` determines which Http client implementation to use. CleverClient supports two implementations out of the box:
- Java's HttpClient (default) via ```JavaHttpClientAdapter```
- Square's OkHttp via ```OkHttpClientAdapter```

| clientAdapter's value                           | Description                         |
|-------------------------------------------------|-------------------------------------|
| new JavaHttpClientAdapter()                     | Uses a default Java's HttpClient    |
| new JavaHttpClientAdapter(customJavaHttpClient) | Uses a custom Java's HttpClient     |
| new OkHttpClientAdapter()                       | Uses a default OkHttpClient         |
| new OkHttpClientAdapter(customOkHttpClient)     | Uses a custom OkHttpClient          |

Example:

```java
final var BASE_URL = "https://api.example.com";
final var HEADER_NAME = "Authorization";
final var HEADER_VALUE = "Bearer qwertyasdfghzxcvb";
final var END_OF_STREAM = "[DONE]";

var httpClient = HttpClient.newBuilder()
    .version(Version.HTTP_1_1)
    .followRedirects(Redirect.NORMAL)
    .connectTimeout(Duration.ofSeconds(20))
    .executor(Executors.newFixedThreadPool(3))
    .proxy(ProxySelector.of(new InetSocketAddress("proxy.example.com", 80)))
    .build();

var objectMapper = new ObjectMapper()
    .registerModule(new JavaTimeModule());

var cleverClient = CleverClient.builder()
    .baseUrl(BASE_URL)
    .header(HEADER_NAME, HEADER_VALUE)
    .bodyInspector(body -> {
        var validator = new Validator();
        var violations = validator.validate(body);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    })
    .requestInterceptor(request -> {
        var url = request.getUrl();
        url + (url.contains("?") ? "&" : "?") + "env=testing";
        request.setUrl(url);
        return request;
    })
    .responseInterceptor(response -> {
        var modifiedBody = customProcessing(response.getBody());
        response.setBody(modifiedBody);
        return response;
    })
    .clientAdapter(new JavaHttpClientAdapter(httpClient))
    .endOfStream(END_OF_STREAM)
    .objectMapper(objectMapper)
    .build();
```

### Interface Annotations

| Annotation | Target     | Attributes                  | Required Attrs | Mult |
|------------|------------|-----------------------------|----------------|------|
| Resource   | Interface  | Resource's url              | optional       | One  |
| Header     | Interface  | Header's name and value     | mandatory both | Many |
| Header     | Method     | Header's name and value     | mandatory both | Many |
| GET        | Method     | GET endpoint's url          | optional       | One  |
| POST       | Method     | POST endpoint's url         | optional       | One  |
| PUT        | Method     | PUT endpoint's url          | optional       | One  |
| DELETE     | Method     | DELETE endpoint's url       | optional       | One  |
| PATCH      | Method     | PATCH endpoint's url        | optional       | One  |
| Multipart  | Method     | (None)                      | none           | One  |
| StreamType | Method     | Class type and events array | mandatory both | Many |
| StreamType | Annotation | Class type and events array | mandatory both | Many |
| Path       | Parameter  | Path parameter name in url  | mandatory      | One  |
| Query      | Parameter  | Query parameter name in url | mandatory      | One  |
| Query      | Parameter  | (None for Pojos)            | none           | One  |
| Body       | Parameter  | (None)                      | none           | One  |

* ```Resource``` could be used to separate the repeated part of the endpoints' url in an interface.
* ```Header``` Used to include more headers (pairs of name and value) at interface or method level. It is possible to have multiple Header annotations for the same target.
* ```GET, POST, PUT, DELETE, PATCH``` are used to mark the typical http methods (endpoints).
* ```Multipart``` is used to mark an endpoint with a multipart/form-data request. This is required when you need to upload files.
* ```StreamType``` is used with methods whose return type is Stream of [Event](./src/main/java/io/github/sashirestela/cleverclient/Event.java). Tipically you will use more than one of this annotation to indicate what classes (types) are related to what events (array of Strings). You can also use them for custom annotations in case you want to reuse them for many methods, so you just apply the custom composite annotation.
* ```Path``` is used to replace the path parameter name in url with the matched method parameter's value.
* ```Query``` is used to add a query parameter to the url in the way: [?]queryValue=parameterValue[&...] for scalar parameters. Also it can be used for POJOs using its properties and values.
* ```Body``` is used to mark a method parameter as the endpoint's payload request, so the request will be application/json at least the endpoint is annotated with Multipart.
* Check the above [Description's example](#-description) or the [Test](https://github.com/sashirestela/cleverclient/tree/main/src/test/java/io/github/sashirestela/cleverclient) folder to see more of these interface annotations in action.

### Supported Response Types

The reponse types are determined from the method's return types. We have six response types: [Stream](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/stream/Stream.html) of elements, [List](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/List.html) of elements, [Generic](https://docs.oracle.com/javase/tutorial/java/generics/types.html) type, Custom type, [Binary](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/io/InputStream.html) type, [String](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/String.html) type and Stream of [Event](./src/main/java/io/github/sashirestela/cleverclient/Event.java), and all of them can be asynchronous or synchronous. For async responses you have to use the Java class [CompletableFuture](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/concurrent/CompletableFuture.html).

| Response Type                      | Sync/Async | Description                 |
|------------------------------------|------------|-----------------------------|
| CompletableFuture<Stream\<T>>      | Async      | SSE (*) as Stream of type T |
| Stream\<T>                         | Sync       | SSE (*) as Stream of type T |
| CompletableFuture<List\<T>>        | Async      | List of type T              |
| List\<T>                           | Sync       | List of type T              |
| CompletableFuture<Generic\<T>>     | Async      | Generic class of type T     |
| Generic\<T>                        | Sync       | Generic class of type T     |
| CompletableFuture\<T>              | Async      | Custom class T              |
| T                                  | Sync       | Custom class T              |
| CompletableFuture\<InputStream>    | Async      | Binary type                 |
| InputStream                        | Sync       | Binary type                 |
| CompletableFuture\<String>         | Async      | String type                 |
| String                             | Sync       | String type                 |
| CompletableFuture<Stream\<Event>>  | Async      | SSE (*) as Stream of Event  |
| Stream\<Event>                     | Sync       | SSE (*) as Stream of Event  |

(*) SSE: Server Sent Events

* ```CompletableFuture<Stream<T>>``` and ```Stream<T>``` are used for handling SSE without events and data of the class ```T``` only.
* ```CompletableFuture<Stream<Event>>``` and ```Stream<Event>``` are used for handling SSE with multiple events and data of different classes.
* The [Event](./src/main/java/io/github/sashirestela/cleverclient/Event.java) class will bring for each event: the event name and the data object.

### Interface Default Methods

You can create interface default methods to execute special requirements such as pre/post processing before/after calling annotated regular methods. For example in the following interface definition, we have two regular methods with POST annotation which are called from another two default methods. In those defaults methods we are making some pre processing (in this case, modifying the request object) before calling the annotated methods:

```java
@Resource("/v1/chat/completions")
interface Completions {

    @POST
    Stream<ChatResponse> createSyncStreamBasic(@Body ChatRequest chatRequest);

    @POST
    CompletableFuture<Stream<ChatResponse>> createAsyncStreamBasic(@Body ChatRequest chatRequest);

    default Stream<ChatResponse> createSyncStream(ChatRequest chatRequest) {
        var request = chatRequest.withStream(true);
        return createSyncStreamBasic(request);
    }

    default CompletableFuture<Stream<ChatResponse>> createAsyncStream(ChatRequest chatRequest) {
        var request = chatRequest.withStream(true);
        return createAsyncStreamBasic(request);
    }

}
```

Note that we have named the annotated methods with the suffix "Basic" just to indicate that we should not call them directly but should call the default ones (those without the suffix).

### Exception Handling

CleverClient provides a flexible exception handling mechanism through the `ExceptionConverter` abstract class. This allows you to convert HTTP errors and other exceptions into your own custom exceptions. Here's how to use it:

1. Create your custom HTTP exception class.
2. Create your exception converter by extending `ExceptionConverter`.
3. Implement the `convertHttpException` method to handle HTTP errors.

Basic example:

```java
// Custom HTTP Exceptions
public class FirstHttpException extends RuntimeException {
    private final String errorDetail;

    public FirstHttpException(String errorDetail) {
        this.errorDetail = errorDetail;
    }
    
    // Getters
}

public class SecondHttpException extends RuntimeException {
    private final String errorDetail;

    public SecondHttpException(String errorDetail) {
        this.errorDetail = errorDetail;
    }
    
    // Getters
}

// Custom Exception Converter
public class MyExceptionConverter extends ExceptionConverter {
    public static void rethrow(Throwable e) {
        throw new MyExceptionConverter().convert(e);
    }

    @Override
    public RuntimeException convertHttpException(ResponseInfo responseInfo) {
        if (responseInfo.getStatusCode() == 400) {
            return new FirstHttpException(responseInfo.getData());
        } else if (responseInfo.getStatusCode() == 401) {
            return new SecondHttpException(responseInfo.getData());
        }
    }
}

// Usage in try-catch block
try {
    // Your CleverClient API calls here
} catch (Exception e) {
    try {
        MyExceptionConverter.rethrow(e);
    } catch (FirstHttpException fhe) {
        // Handle your first custom exception
    } catch (SecondHttpException she) {
        // Handle your second custom exception

    // ... Other custom exceptions

    } catch (RuntimeException re) {
        // Handle default exceptions
    }
}
```

This mechanism allows you to handle both HTTP errors and other runtime exceptions in a clean, consistent way while preserving the original error information from the API response.

### WebSocket

We have the following attributes to create a CleverClient.WebSocket object:

| Attribute          | Description                                                  | Required  |
| -------------------|--------------------------------------------------------------|-----------|
| baseUrl            | WebSocket's url                                              | mandatory |
| queryParams        | Map of query params (name/value)                             | optional  |
| queryParam         | Single query param as a name and a value                     | optional  |
| headers            | Map of headers (name/value)                                  | optional  |
| header             | Single header as a name and a value                          | optional  |
| webSocketAdapter   | WebSocket implementation (Java HttpClient or OkHttp based)   | optional  |

The attribute ```webSocketAdapter``` lets you specify which WebSocket implementation to use. You can choose between:
- Java's HttpClient (default) via ```JavaHttpWebSocketAdapter```
- Square's OkHttp via ```OkHttpWebSocketAdapter```

| webSocketAdapter's value                           | Description                         |
|----------------------------------------------------|-------------------------------------|
| new JavaHttpWebSocketAdapter()                     | Uses a default Java's HttpClient    |
| new JavaHttpWebSocketAdapter(customJavaHttpClient) | Uses a custom Java's HttpClient     |
| new OkHttpWebSocketAdapter()                       | Uses a default OkHttpClient         |
| new OkHttpWebSocketAdapter(customOkHttpClient)     | Uses a custom OkHttpClient          |

Example:

```java
final var BASE_URL = "ws://websocket.example.com";
final var HEADER_NAME = "Authorization";
final var HEADER_VALUE = "Bearer qwertyasdfghzxcvb";

var httpClient = HttpClient.newBuilder()
    .version(Version.HTTP_1_1)
    .followRedirects(Redirect.NORMAL)
    .connectTimeout(Duration.ofSeconds(20))
    .executor(Executors.newFixedThreadPool(3))
    .proxy(ProxySelector.of(new InetSocketAddress("proxy.example.com", 80)))
    .build();

var webSocket = CleverClient.WebSocket.builder()
    .baseUrl(BASE_URL)
    .queryParam("model", "qwerty_model")
    .header(HEADER_NAME, HEADER_VALUE)
    .webSocketAdapter(new JavaHttpWebSocketAdapter(httpClient))
    .build();

webSocket.onOpen(() -> System.out.println("Connected"));
webSocket.onMessage(message -> System.out.println("Received: " + message));
webSocket.onClose((code, message) -> System.out.println("Closed"));

webSocket.connect().join();
webSocket.send("Hello World!").join();
webSocket.send("Welcome to the Jungle!").join();
webSocket.close();
```


## ✳ Examples

Some examples have been created in the folder [example](https://github.com/sashirestela/cleverclient/tree/main/src/example/java/io/github/sashirestela/cleverclient/example) and you can follow the next steps to execute them:

* Clone this respository:

  ```sh
  git clone https://github.com/sashirestela/cleverclient.git
  cd cleverclient
  ```

* Build the project:

  ```sh
  mvn clean install
  ```

* Run example:

  ```sh
  mvn exec:java -Dexec.mainClass=io.github.sashirestela.cleverclient.example.<className> [logOptions]
  ```

  Where:

  * ```<className>``` is mandatory and must be one of the Java files in the folder example: BasicExample, BasicExampleOkHttp, StreamExample, StreamExampleOkHttp, etc.
  
    Some examples require you have an OpenAI account and set the env variable OPENAI_API_TOKEN, such as Multipart*, Stream*.
  
  * ```[logOptions]``` are optional and you can you use them to set:
    * Logger lever: ```-Dorg.slf4j.simpleLogger.defaultLogLevel=<logLevel>```
    * Logger file: ```-Dorg.slf4j.simpleLogger.logFile=<logFile>```
  * For example, to run the BasicExample with all the log options:
    * ```mvn exec:java -Dexec.mainClass=io.github.sashirestela.cleverclient.example.BasicExample -Dorg.slf4j.simpleLogger.defaultLogLevel=debug -Dorg.slf4j.simpleLogger.logFile=example.log```

## 💼 Contributing
Please read our [Contributing](CONTRIBUTING.md) guide to learn and understand how to contribute to this project.

## 📄 License
CleverClient is licensed under the MIT License. See the
[LICENSE](https://github.com/sashirestela/cleverclient/blob/main/LICENSE) file
for more information.

## ❤ Show Us Your Love
Thanks for using **cleverclient**. If you find this project valuable there are a few ways you can show us your love, preferably all of them 🙂:

* Letting your friends know about this project 🗣📢.
* Writing a brief review on your social networks ✍🌐.
* Giving us a star on Github ⭐.
