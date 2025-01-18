package io.github.sashirestela.cleverclient.example.jsonplaceholder;

import io.github.sashirestela.cleverclient.annotation.GET;
import io.github.sashirestela.cleverclient.annotation.Query;
import io.github.sashirestela.cleverclient.annotation.Resource;

import java.util.List;

@Resource("/users")
public interface UserService {

    @GET
    List<User> readUsers(@Query("_page") Integer page, @Query("_limit") Integer limit);

}
