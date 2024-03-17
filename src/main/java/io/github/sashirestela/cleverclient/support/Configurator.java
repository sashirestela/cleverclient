package io.github.sashirestela.cleverclient.support;

import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

@Getter
public class Configurator {

    private static Configurator configurator;

    private List<String> eventsToRead;
    private List<String> endsOfStream;

    private Configurator() {
    }

    @Builder
    public Configurator(@Singular("eventToRead") List<String> eventsToRead,
            @Singular("endOfStream") List<String> endsOfStream) {
        if (configurator != null) {
            return;
        }
        configurator = new Configurator();
        configurator.eventsToRead = eventsToRead;
        configurator.endsOfStream = endsOfStream;
    }

    public static Configurator one() {
        if (configurator == null) {
            throw new CleverClientException("You have to call Configurator.builder() first.");
        }
        return configurator;
    }
}
