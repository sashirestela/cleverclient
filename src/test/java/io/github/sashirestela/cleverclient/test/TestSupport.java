package io.github.sashirestela.cleverclient.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.sashirestela.cleverclient.support.Configurator;

public class TestSupport {

    private TestSupport() {
    }

    public static void setupConfigurator() {
        Configurator.reset();
        Configurator.builder()
                .endOfStream("END")
                .objectMapper(new ObjectMapper().registerModule(new JavaTimeModule()))
                .build();
    }

    public enum SyncType {
        SYNC,
        ASYNC;
    }

}
