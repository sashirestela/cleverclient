package io.github.sashirestela.cleverclient.support;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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

    @Test
    void shouldBringOutTheCleverClientExceptionIfItExists() {
        Object[][] testData = {
                { new CleverClientException("Outer Exception"), true },
                { new Exception("Outer Exception", new CleverClientException("Inner Exception")), true },
                { new Exception("No CleverClientException"), false }
        };
        for (Object[] data : testData) {
            var optionalException = CleverClientException.getFrom((Throwable) data[0]);
            var expectedCondition = (boolean) data[1];
            var actualCondition = optionalException.isPresent();
            assertEquals(expectedCondition, actualCondition);
            if (expectedCondition) {
                assertEquals(CleverClientException.class, optionalException.get().getClass());
            }
        }
    }

}
