package io.github.sashirestela.cleverclient.example.jsonplaceholder;

import io.github.sashirestela.cleverclient.annotation.Body;
import io.github.sashirestela.cleverclient.annotation.DELETE;
import io.github.sashirestela.cleverclient.annotation.GET;
import io.github.sashirestela.cleverclient.annotation.PATCH;
import io.github.sashirestela.cleverclient.annotation.POST;
import io.github.sashirestela.cleverclient.annotation.PUT;
import io.github.sashirestela.cleverclient.annotation.Path;
import io.github.sashirestela.cleverclient.annotation.Query;
import io.github.sashirestela.cleverclient.annotation.Resource;

import java.util.List;

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

    @PATCH("/{postId}")
    Post patchPost(@Path("postId") Integer postId, @Body Post post);

    @DELETE("/{postId}")
    Post deletePost(@Path("postId") Integer postId);

}
