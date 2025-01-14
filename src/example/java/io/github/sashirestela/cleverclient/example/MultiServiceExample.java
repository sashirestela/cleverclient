package io.github.sashirestela.cleverclient.example;

import io.github.sashirestela.cleverclient.CleverClient;
import io.github.sashirestela.cleverclient.example.jsonplaceholder.AlbumService;
import io.github.sashirestela.cleverclient.example.jsonplaceholder.PostService;

public class MultiServiceExample extends AbstractExample {

    public MultiServiceExample(String clientAlias) {
        super(clientAlias);
    }

    public MultiServiceExample() {
        this("javahttp");
    }

    public void run() {
        var cleverClient = CleverClient.builder()
                .baseUrl("https://jsonplaceholder.typicode.com")
                .clientAdapter(clientAdapter)
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

    public static void main(String[] args) {
        var example = new MultiServiceExample();
        example.run();
    }

}
