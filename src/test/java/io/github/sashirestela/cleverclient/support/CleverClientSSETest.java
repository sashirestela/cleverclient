package io.github.sashirestela.cleverclient.support;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CleverClientSSETest {

  @Test
  void shouldReturnExpectedValueWhenRawDataHasDifferentValues() {
    Object[][] testData = {
        { new CleverClientSSE("data: This is the actual data."), true },
        { new CleverClientSSE("data : This is the actual data."), false },
        { new CleverClientSSE("\n"), false },
        { new CleverClientSSE(""), false }
    };
    for (Object[] data : testData) {
      CleverClientSSE event = (CleverClientSSE) data[0];
      boolean actualCondition = event.isActualData();
      boolean expectedCondition = (boolean) data[1];
      assertEquals(expectedCondition, actualCondition);
    }
  }

  @Test
  @SuppressWarnings("unused")
  void shouldReturnTheActualDataWhenRawDataMeetsConditions() {
    CleverClientSSE event = new CleverClientSSE("data:   This is the actual data.  ");
    String rawData = event.getRawData();
    String actualData = event.getActualData();
    String expectedData = "This is the actual data.";
    assertEquals(expectedData, actualData);
  }
}