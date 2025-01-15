package io.github.sashirestela.cleverclient.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConfiguratorTest {

    @BeforeAll
    static void setup() {
        Configurator.reset();
    }

    @Test
    void testBuilder() {
        assertThrows(CleverClientException.class, () -> Configurator.one());
        Configurator.builder()
                .endOfStream("END")
                .objectMapper(new ObjectMapper().registerModule(new JavaTimeModule()))
                .build();
        assertDoesNotThrow(() -> Configurator.one());
    }

}
