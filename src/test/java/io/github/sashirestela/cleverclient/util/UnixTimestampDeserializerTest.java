package io.github.sashirestela.cleverclient.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class UnixTimestampDeserializerTest {

    @Test
    void testDeserialize() {
        var jsonData = "{\"id\":1,\"description\":\"sample data\",\"time\":1700671053}";
        var expectedTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(1700671053), ZoneId.systemDefault());
        var actualObject = JsonUtil.jsonToObject(jsonData, SampleClass.class);
        assertEquals(expectedTime, actualObject.getTime());
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    static class SampleClass {

        private Integer id;

        private String description;

        @JsonDeserialize(using = UnixTimestampDeserializer.class)
        private ZonedDateTime time;

    }
}