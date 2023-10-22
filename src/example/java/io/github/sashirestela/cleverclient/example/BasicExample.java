package io.github.sashirestela.cleverclient.example;

import io.github.sashirestela.cleverclient.CleverClient;
import io.github.sashirestela.cleverclient.example.jsonplaceholder.Post;
import io.github.sashirestela.cleverclient.example.jsonplaceholder.PostService;

public class BasicExample {

  public static void main(String[] args) {
    final var URL_BASE = "https://jsonplaceholder.typicode.com";

    var cleverClient = CleverClient.builder()
        .urlBase(URL_BASE)
        .build();
    var postService = cleverClient.create(PostService.class);

    var page = 1;
    var limit = 5;
    var postId = 17;
    var userId = 3;

    showTitle("Example Read Posts");
    var postsList = postService.readPosts(page, limit);
    postsList.forEach(System.out::println);

    showTitle("Example Read Post");
    var onePost = postService.readPost(postId);
    System.out.println(onePost);

    showTitle("Example Create Post");
    var newPost = postService.createPost(Post.builder()
        .title("Hello")
        .body("Hello word, you are very welcome!")
        .userId(userId)
        .build());
    System.out.println(newPost);

    showTitle("Example Update Post");
    var editPost = postService.updatePost(postId, Post.builder()
        .title("Hello")
        .body("Hello word, you are very welcome!")
        .userId(userId)
        .build());
    System.out.println(editPost);

    showTitle("Example Delete Post");
    postService.deletePost(postId);
    System.out.println("Post was deleted");
  }

  private static void showTitle(String title) {
    final var times = 50;
    System.out.println("=".repeat(times));
    System.out.println(title);
    System.out.println("-".repeat(times));
  }
}