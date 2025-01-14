package io.github.sashirestela.cleverclient.example.openai;

import io.github.sashirestela.cleverclient.annotation.Body;
import io.github.sashirestela.cleverclient.annotation.Multipart;
import io.github.sashirestela.cleverclient.annotation.POST;
import io.github.sashirestela.cleverclient.annotation.Resource;

@Resource("/v1/audio")
public interface AudioService {

    @Multipart
    @POST("/transcriptions")
    Transcription transcribe(@Body TranscriptionRequest audioRequest);

}
