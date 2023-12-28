package io.github.sashirestela.cleverclient.example.jsonplaceholder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@JsonInclude(Include.NON_NULL)
public class Post {
    private Integer id;
    private String title;
    private String body;
    private Integer userId;
}