package io.github.sashirestela.cleverclient;

import lombok.Builder;
import lombok.Value;

/**
 * Represents every event in a Server Sent Event interaction.
 */
@Value
@Builder
public class Event {

    String name;
    Object data;

}
