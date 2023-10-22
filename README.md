[![codecov](https://codecov.io/gh/sashirestela/cleverclient/graph/badge.svg?token=PEYAFW3EWD)](https://codecov.io/gh/sashirestela/cleverclient)
![Maven Central](https://img.shields.io/maven-central/v/io.github.sashirestela/cleverclient)
![GitHub Workflow Status (with event)](https://img.shields.io/github/actions/workflow/status/sashirestela/cleverclient/maven.yml)

# 💎 CleverClient
Java library that makes it easier to use the Java's HttpClient to perform http operations, using interfaces.


## 💡 Description
CleverClient is a Java 11+ library that makes it easy to use the standard [HttpClient](https://docs.oracle.com/en/java/javase/11/docs/api/java.net.http/java/net/http/HttpClient.html) component to call http services by using annotated interfaces.

For example, if we want to call the API [JsonPlaceHolder](https://jsonplaceholder.typicode.com/) with the endpoint ```/posts```, we just have to create an entity ```Post```, an interface ```PostService``` with special annotatons, and call the API through ```CleverClient```:

```
// Entity
public class Post {
  private Integer id;
  private String title;
  private String body;
  private Integer userId;

  // Constructors , getters, setters, etc.
}

// Interface
public interface PostService {

  @GET("/posts")
  List<Post> readPosts(@Query("_page") Integer page, @Query("_limit") Integer limit);

  @GET("/posts/{postId}")
  Post readPost(@Path("postId") Integer postId);

  @POST("/posts")
  Post createPost(@Body Post post);

}

// Use CleverClient to call the API
var cleverClient = CleverClient.builder()
    .urlBase("https://jsonplaceholder.typicode.com/")
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


## 🛠️ Installation
You can install CleverClient by adding the following dependency to your Maven project:

```xml
<dependency>
    <groupId>io.github.sashirestela</groupId>
    <artifactId>cleverclient</artifactId>
    <version>[latest version]</version>
</dependency>
```

Or alternatively using Gradle:

```groovy
dependencies {
    implementation 'io.github.sashirestela:cleverclient:[latest version]'
}
```

Take in account that you need to use **Java 11 or above**.


## 📕 Features

### CleverClient Builder
We have the following attributes to create a CleverClient object:

| Attribute   | Description                        | Required  |
| ----------- |------------------------------------|-----------|
| urlBase     | Api's url                          | mandatory |
| headers     | Pairs of headers name/value        | optional  |
| httpClient  | Java HttpClient object             | optional  |
| endOfStream | Text used to mark final of streams | optional  |

Example:

```
final var URL_BASE = "https://api.example.com";
final var AUTHORIZATION_HEADER = "Authorization";
final var BEARER_AUTHORIZATION = "Bearer qwertyasdfghzxcvb";
final var END_OF_STREAM = "[DONE]";

var httpClient = HttpClient.newBuilder()
    .version(Version.HTTP_1_1)
    .followRedirects(Redirect.NORMAL)
    .connectTimeout(Duration.ofSeconds(20))
    .executor(Executors.newFixedThreadPool(3))
    .proxy(ProxySelector.of(new InetSocketAddress("proxy.example.com", 80)))
    .build();

var cleverClient = CleverClient.builder()
    .urlBase(URL_BASE)
    .headers(Arrays.asList(AUTHORIZATION_HEADER, BEARER_AUTHORIZATION))
    .httpClient(httpClient)
    .endOfStream(END_OF_STREAM)
    .build();
```

### Interface Annotations

| Annotation | Target    | Value                    |
|------------|-----------|--------------------------|
| Resource   | Interface | Resource's url           |
| GET        | Method    | GET endpoint's url       |
| POST       | Method    | POST endpoint's url      |
| PUT        | Method    | PUT endpoint's url       |
| DELETE     | Method    | DELETE endpoint's url    |
| Multipart  | Method    | None. Mark for multipart |
| Path       | Parameter | Path parameter name      |
| Query      | Parameter | Query parameter name     |


## ✳ Examples
Some examples have been created in the folder [example](https://github.com/sashirestela/cleverclient/tree/main/src/example/java/io/github/sashirestela/cleverclient/example) and you can follow the next steps to execute them:
* Clone this respository:
  ```
  git clone https://github.com/sashirestela/cleverclient.git
  cd cleverclient
  ```
* Build the project:
  ```
  mvn clean install
  ```
* Run example:
  ```
  mvn exec:java -Dexec.mainClass=io.github.sashirestela.cleverclient.example.<className> [logOptions]
  ```
  Where:

  * ```<className>``` is mandatory and must be one of the values:
    * BasicExample
    * StreamExample (This requires you have an OpenAI account and set the env variable OPENAI_API_TOKEN)
  
  * ```[logOptions]``` are optional and you can you use them to set:
    * Logger lever: ```-Dorg.slf4j.simpleLogger.defaultLogLevel=<logLevel>```
    * Logger file: ```-Dorg.slf4j.simpleLogger.logFile=<logFile>```
  * For example, to run the BasicExample with all the log options:
    * ```mvn exec:java -Dexec.mainClass=io.github.sashirestela.cleverclient.example.BasicExample -Dorg.slf4j.simpleLogger.defaultLogLevel=debug -Dorg.slf4j.simpleLogger.logFile=example.log```


## 📄 License
CleverClient is licensed under the MIT License. See the
[LICENSE](https://github.com/sashirestela/cleverclient/blob/main/LICENSE) file
for more information.