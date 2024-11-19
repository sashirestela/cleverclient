package io.github.sashirestela.cleverclient.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Order(1)
class ConfiguratorTest {

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
