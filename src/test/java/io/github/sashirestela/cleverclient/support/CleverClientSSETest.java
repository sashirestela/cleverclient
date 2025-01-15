package io.github.sashirestela.cleverclient.support;

import io.github.sashirestela.cleverclient.support.CleverClientSSE.LineRecord;
import io.github.sashirestela.cleverclient.test.TestSupport;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CleverClientSSETest {

    Set<String> events = Set.of("process", "process2");

    @BeforeAll
    static void setup() {
        TestSupport.setupConfigurator();
    }

    @Test
    void shouldReturnExpectedValueWhenRawDataMeetsConditions() {
        Object[][] testData = {
                { new CleverClientSSE(new LineRecord("event: process", "data: Actual data."), events), true },
                { new CleverClientSSE(new LineRecord("", "data: Actual data.")), true },
                { new CleverClientSSE(new LineRecord("event: other", "data: Actual data."), events), false },
                { new CleverClientSSE(new LineRecord("event: process", "data : Actual data."), events), false },
                { new CleverClientSSE(new LineRecord("", "data : Actual data.")), false },
                { new CleverClientSSE(new LineRecord("", "\n")), false },
                { new CleverClientSSE(new LineRecord("", "")), false },
                { new CleverClientSSE(new LineRecord("event: process", "data: END"), events), false },
                { new CleverClientSSE(new LineRecord("", "data: END")), false }
        };
        for (Object[] data : testData) {
            var event = (CleverClientSSE) data[0];
            var actualCondition = event.isActualData();
            var expectedCondition = (boolean) data[1];
            assertEquals(expectedCondition, actualCondition, "For data '" + event.getActualData()
                    + "' was expecting " + expectedCondition + " but was " + actualCondition);
        }
    }

    @Test
    void shouldReturnCleanDataWhenRawDataMeetsConditions() {
        CleverClientSSE event = new CleverClientSSE(
                new LineRecord("event: process", "data:   This is the actual data.  "));
        var actualData = event.getActualData();
        var expectedData = "This is the actual data.";
        assertEquals(expectedData, actualData);
    }

    @Test
    void shouldReturnExpectedMatcheEventWhenRawDataMeetsConditions() {
        Object[][] testData = {
                { new CleverClientSSE(new LineRecord("event: process", "data: Actual data."), events), "process" },
                { new CleverClientSSE(new LineRecord("event: other", "data: Actual data."), events), null },
                { new CleverClientSSE(new LineRecord("", "data: Actual data.")), "" }
        };
        for (Object[] data : testData) {
            var event = (CleverClientSSE) data[0];
            var actualMatchedEvent = event.getMatchedEvent();
            var expectedMatchedEvent = (String) data[1];
            assertEquals(expectedMatchedEvent, actualMatchedEvent);
        }

    }

}
