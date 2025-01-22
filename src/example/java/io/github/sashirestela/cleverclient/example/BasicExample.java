package io.github.sashirestela.cleverclient.example;

import io.github.sashirestela.cleverclient.CleverClient;
import io.github.sashirestela.cleverclient.example.jsonplaceholder.Post;
import io.github.sashirestela.cleverclient.example.jsonplaceholder.PostService;

public class BasicExample extends AbstractExample {

    public BasicExample(String clientAlias) {
        super(clientAlias);
    }

    public BasicExample() {
        this("javahttp");
    }

    public void run() {
        var cleverClient = CleverClient.builder()
                .baseUrl("https://jsonplaceholder.typicode.com")
                .clientAdapter(clientAdapter)
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

        showTitle("Example Patch Post");
        var patchPost = postService.patchPost(postId, Post.builder()
                .title("Godbye")
                .build());
        System.out.println(patchPost);

        showTitle("Example Delete Post");
        postService.deletePost(postId);
        System.out.println("Post was deleted");
    }

    public static void main(String[] args) {
        var example = new BasicExample();
        example.run();
    }

}
