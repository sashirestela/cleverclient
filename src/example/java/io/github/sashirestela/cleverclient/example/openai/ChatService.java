package io.github.sashirestela.cleverclient.example.openai;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import io.github.sashirestela.cleverclient.annotation.Body;
import io.github.sashirestela.cleverclient.annotation.POST;
import io.github.sashirestela.cleverclient.annotation.Resource;

@Resource("/v1/chat/completions")
public interface ChatService {

    @POST
    Stream<ChatResponse> __createSyncStream(@Body ChatRequest chatRequest);

    default Stream<ChatResponse> createSyncStream(ChatRequest chatRequest) {
        var request = chatRequest.withStream(true);
        return __createSyncStream(request);
    }

    @POST
    CompletableFuture<Stream<ChatResponse>> __createAsyncStream(@Body ChatRequest chatRequest);

    default CompletableFuture<Stream<ChatResponse>> createAsyncStream(ChatRequest chatRequest) {
        var request = chatRequest.withStream(true);
        return __createAsyncStream(request);
    }

}