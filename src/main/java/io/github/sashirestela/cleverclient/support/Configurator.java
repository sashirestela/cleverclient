package io.github.sashirestela.cleverclient.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.util.List;

@Getter
public class Configurator {

    private static Configurator configurator = new Configurator();

    private List<String> endsOfStream;
    private ObjectMapper objectMapper;

    @Getter(AccessLevel.NONE)
    private boolean wasBuilt = false;

    private Configurator() {
    }

    @Builder
    public Configurator(@Singular("endOfStream") List<String> endsOfStream, ObjectMapper objectMapper) {
        if (configurator.wasBuilt) {
            return;
        }
        configurator.endsOfStream = endsOfStream;
        configurator.objectMapper = objectMapper;
        configurator.wasBuilt = true;
    }

    public static Configurator one() {
        if (!configurator.wasBuilt) {
            throw new CleverClientException("You have to call Configurator.builder() first.");
        }
        return configurator;
    }

}
