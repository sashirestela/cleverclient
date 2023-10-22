package io.github.sashirestela.cleverclient.example.openai;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@Getter
@ToString
public class ChatResponse {
  private String id;
  private String object;
  private Long created;
  private String model;
  private List<Choice> choices;
  private Usage usage;

  public String firstContent() {
    return getChoices().get(0).getMessage().getContent();
  }
}