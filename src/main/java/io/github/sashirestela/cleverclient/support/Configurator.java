package io.github.sashirestela.cleverclient.support;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.util.List;
import java.util.stream.Collectors;

public class Configurator {

    private static final String EVENT_HEADER = "event: ";
    private static final String SEPARATOR = "";

    private static Configurator configurator = new Configurator();

    @Getter
    private List<String> eventsToRead;
    @Getter
    private List<String> endsOfStream;

    private List<String> linesToCheck;
    private boolean wasBuilt = false;

    private Configurator() {
    }

    @Builder
    public Configurator(@Singular("eventToRead") List<String> eventsToRead,
            @Singular("endOfStream") List<String> endsOfStream) {
        if (configurator.wasBuilt) {
            return;
        }
        configurator.eventsToRead = eventsToRead;
        configurator.endsOfStream = endsOfStream;
        configurator.wasBuilt = true;
    }

    public static Configurator one() {
        if (!configurator.wasBuilt) {
            throw new CleverClientException("You have to call Configurator.builder() first.");
        }
        return configurator;
    }

    public List<String> getLinesToCheck() {
        if (linesToCheck == null) {
            linesToCheck = eventsToRead.stream()
                    .filter(etr -> !etr.isEmpty())
                    .map(etr -> (EVENT_HEADER + etr))
                    .collect(Collectors.toList());
            linesToCheck.add(SEPARATOR);
        }
        return linesToCheck;
    }

}
