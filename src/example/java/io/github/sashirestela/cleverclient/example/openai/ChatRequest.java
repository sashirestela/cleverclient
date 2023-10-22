package io.github.sashirestela.cleverclient.example.openai;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder
public class ChatRequest {
  private String model;
  private List<Message> messages;
  private Double temperature;
  private Boolean stream;
}