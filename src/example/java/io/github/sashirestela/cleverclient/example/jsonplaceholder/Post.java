package io.github.sashirestela.cleverclient.example.jsonplaceholder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class Post {
  private Integer id;
  private String title;
  private String body;
  private Integer userId;
}