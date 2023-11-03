package io.github.sashirestela.cleverclient.example.jsonplaceholder;

import java.util.List;

import io.github.sashirestela.cleverclient.annotation.Body;
import io.github.sashirestela.cleverclient.annotation.DELETE;
import io.github.sashirestela.cleverclient.annotation.GET;
import io.github.sashirestela.cleverclient.annotation.POST;
import io.github.sashirestela.cleverclient.annotation.PUT;
import io.github.sashirestela.cleverclient.annotation.Path;
import io.github.sashirestela.cleverclient.annotation.Query;
import io.github.sashirestela.cleverclient.annotation.Resource;

@Resource("/posts")
public interface PostService {

    @GET
    List<Post> readPosts(@Query("_page") Integer page, @Query("_limit") Integer limit);

    @GET("/{postId}")
    Post readPost(@Path("postId") Integer postId);

    @POST
    Post createPost(@Body Post post);

    @PUT("/{postId}")
    Post updatePost(@Path("postId") Integer postId, @Body Post post);

    @DELETE("/{postId}")
    Post deletePost(@Path("postId") Integer postId);

}