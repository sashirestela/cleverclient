package io.github.sashirestela.cleverclient.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class CleverClientExceptionTest {

  @Test
  void shouldReplaceParametersInErrorMessageWhenAnExceptionIsCreated() {
    CleverClientException exception = new CleverClientException("{0}, {1}", "parameter1", "parameter2", null);
    String actualExceptionMessage = exception.getMessage();
    String expectedExceptionMessage = "parameter1, parameter2";
    assertEquals(expectedExceptionMessage, actualExceptionMessage);
  }

  @Test
  void shouldSetCauseInErrorWhenItIsPassedAsLastArgumentAtExceptionCreation() {
    CleverClientException exception = new CleverClientException("Message", null, new Exception());
    assertNotNull(exception.getCause());
  }

}