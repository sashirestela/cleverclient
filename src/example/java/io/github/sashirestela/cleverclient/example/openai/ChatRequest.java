package io.github.sashirestela.cleverclient.example.openai;

import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.With;

@Getter
@Builder
public class ChatRequest {
    private String model;
    private List<Message> messages;
    private Double temperature;
    @With
    private Boolean stream;
}