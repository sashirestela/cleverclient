package io.github.sashirestela.cleverclient.example.openai;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.nio.file.Path;
import java.util.Set;

@Getter
@Builder
@JsonInclude(Include.NON_EMPTY)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class TranscriptionRequest {

    private Path file;

    private String model;

    private String language;

    private String prompt;

    private AudioResponseFormat responseFormat;

    private Double temperature;

    @Singular
    private Set<TimestampGranularity> timestampGranularities;

    public enum TimestampGranularity {

        @JsonProperty("word")
        WORD,

        @JsonProperty("segment")
        SEGMENT;

    }

    public enum AudioResponseFormat {

        @JsonProperty("json")
        JSON,

        @JsonProperty("text")
        TEXT,

        @JsonProperty("srt")
        SRT,

        @JsonProperty("verbose_json")
        VERBOSE_JSON,

        @JsonProperty("vtt")
        VTT;

    }

}
