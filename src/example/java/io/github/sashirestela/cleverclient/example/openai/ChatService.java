package io.github.sashirestela.cleverclient.example.openai;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import io.github.sashirestela.cleverclient.annotation.Body;
import io.github.sashirestela.cleverclient.annotation.POST;
import io.github.sashirestela.cleverclient.annotation.Resource;

@Resource("/v1/chat/completions")
public interface ChatService {
  
  @POST
  Stream<ChatResponse> createSyncStream(@Body ChatRequest chatRequest);

  @POST
  CompletableFuture<Stream<ChatResponse>> createAsyncStream(@Body ChatRequest chatRequest);

}