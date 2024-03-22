package io.github.sashirestela.cleverclient;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Event {

    String name;
    Object data;

}
