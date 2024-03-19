package io.github.sashirestela.cleverclient.example.jsonplaceholder;

import io.github.sashirestela.cleverclient.annotation.Body;
import io.github.sashirestela.cleverclient.annotation.DELETE;
import io.github.sashirestela.cleverclient.annotation.GET;
import io.github.sashirestela.cleverclient.annotation.POST;
import io.github.sashirestela.cleverclient.annotation.PUT;
import io.github.sashirestela.cleverclient.annotation.Path;
import io.github.sashirestela.cleverclient.annotation.Query;
import io.github.sashirestela.cleverclient.annotation.Resource;

import java.util.List;

@Resource("/albums")
public interface AlbumService {

    @GET
    List<Album> readAlbums(@Query("_page") Integer page, @Query("_limit") Integer limit);

    @GET("/{albumId}")
    Album readAlbum(@Path("albumId") Integer albumId);

    @POST
    Album createAlbum(@Body Album album);

    @PUT("/{albumId}")
    Album updateAlbum(@Path("albumId") Integer albumId, @Body Album album);

    @DELETE("/{albumId}")
    Album deleteAlbum(@Path("albumId") Integer albumId);

}
