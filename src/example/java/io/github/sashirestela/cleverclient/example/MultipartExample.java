package io.github.sashirestela.cleverclient.example;

import io.github.sashirestela.cleverclient.CleverClient;
import io.github.sashirestela.cleverclient.example.openai.AudioService;
import io.github.sashirestela.cleverclient.example.openai.TranscriptionRequest;
import io.github.sashirestela.cleverclient.example.openai.TranscriptionRequest.AudioResponseFormat;
import io.github.sashirestela.cleverclient.example.openai.TranscriptionRequest.TimestampGranularity;

import java.nio.file.Paths;

public class MultipartExample extends AbstractExample {

    public MultipartExample(String clientAlias) {
        super(clientAlias);
    }

    public MultipartExample() {
        this("javahttp");
    }

    public void run() {
        var cleverClient = CleverClient.builder()
                .baseUrl("https://api.openai.com")
                .clientAdapter(clientAdapter)
                .header("Authorization", "Bearer " + System.getenv("OPENAI_API_KEY"))
                .endOfStream("[DONE]")
                .build();
        var audioService = cleverClient.create(AudioService.class);
        var audioRequest = TranscriptionRequest.builder()
                .file(Paths.get("src/test/resources/hello_audio.mp3"))
                .model("whisper-1")
                .responseFormat(AudioResponseFormat.VERBOSE_JSON)
                .temperature(0.2)
                .timestampGranularity(TimestampGranularity.WORD)
                .timestampGranularity(TimestampGranularity.SEGMENT)
                .build();
        var audioResponse = audioService.transcribe(audioRequest);
        System.out.println(audioResponse.getText());
    }

    public static void main(String[] args) {
        var example = new MultipartExample();
        example.run();
    }

}
