package io.github.sashirestela.cleverclient.support;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.github.sashirestela.cleverclient.support.CleverClientSSE.LineRecord;

class CleverClientSSETest {

    @BeforeAll
    static void setup() {
        Configurator.builder()
                .eventToRead("process")
                .endOfStream("END")
                .build();
    }

    @Test
    void shouldReturnExpectedValueWhenRawDataHasDifferentValues() {
        Object[][] testData = {
                { new CleverClientSSE(new LineRecord("event: process", "data: This is the actual data.")), true },
                { new CleverClientSSE(new LineRecord("", "data: This is the actual data.")), true },
                { new CleverClientSSE(new LineRecord("event: other", "data: This is the actual data.")), false },
                { new CleverClientSSE(new LineRecord("event: process", "data : This is the actual data.")), false },
                { new CleverClientSSE(new LineRecord("", "data : This is the actual data.")), false },
                { new CleverClientSSE(new LineRecord("", "\n")), false },
                { new CleverClientSSE(new LineRecord("", "")), false },
                { new CleverClientSSE(new LineRecord("event: process", "data: END")), false },
                { new CleverClientSSE(new LineRecord("", "data: END")), false }
        };
        for (Object[] data : testData) {
            var event = (CleverClientSSE) data[0];
            var actualCondition = event.isActualData();
            var expectedCondition = (boolean) data[1];
            assertEquals(expectedCondition, actualCondition);
        }
    }

    @Test
    @SuppressWarnings("unused")
    void shouldReturnTheActualDataWhenRawDataMeetsConditions() {
        CleverClientSSE event = new CleverClientSSE(new LineRecord("event: process", "data:   This is the actual data.  "));
        var rawData = event.getLineRecord();
        var actualData = event.getActualData();
        var expectedData = "This is the actual data.";
        assertEquals(expectedData, actualData);
    }
}