package io.github.sashirestela.cleverclient.example;

import io.github.sashirestela.cleverclient.CleverClient;
import io.github.sashirestela.cleverclient.example.jsonplaceholder.AlbumService;
import io.github.sashirestela.cleverclient.example.jsonplaceholder.PostService;

public class MultiServiceExample {

    public static void main(String[] args) {
        final var BASE_URL = "https://jsonplaceholder.typicode.com";

        var cleverClient = CleverClient.builder()
                .baseUrl(BASE_URL)
                .build();
        var postService = cleverClient.create(PostService.class);
        var albumService = cleverClient.create(AlbumService.class);

        var page = 1;
        var limit = 5;
        var postId = 17;

        showTitle("Example Read Posts");
        var postsList = postService.readPosts(page, limit);
        postsList.forEach(System.out::println);

        showTitle("Example Read Albums");
        var albumsList = albumService.readAlbums(page, limit);
        albumsList.forEach(System.out::println);

        showTitle("Example Read Post");
        var post = postService.readPost(postId);
        System.out.println(post);
    }

    private static void showTitle(String title) {
        final var times = 50;
        System.out.println("=".repeat(times));
        System.out.println(title);
        System.out.println("-".repeat(times));
    }
}