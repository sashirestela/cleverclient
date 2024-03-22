package io.github.sashirestela.cleverclient.support;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.util.List;

public class Configurator {

    private static Configurator configurator = new Configurator();

    @Getter
    private List<String> endsOfStream;

    private boolean wasBuilt = false;

    private Configurator() {
    }

    @Builder
    public Configurator(@Singular("endOfStream") List<String> endsOfStream) {
        if (configurator.wasBuilt) {
            return;
        }
        configurator.endsOfStream = endsOfStream;
        configurator.wasBuilt = true;
    }

    public static Configurator one() {
        if (!configurator.wasBuilt) {
            throw new CleverClientException("You have to call Configurator.builder() first.");
        }
        return configurator;
    }

}
