package io.github.sashirestela.cleverclient;

import io.github.sashirestela.cleverclient.support.CleverClientException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class ExceptionConverterTest {

    private ExceptionConverter exceptionConverter;

    @Mock
    private ResponseInfo mockResponseInfo;

    @BeforeEach
    void setUp() {
        exceptionConverter = new ExceptionConverter() {

            @Override
            public RuntimeException convertHttpException(ResponseInfo responseInfo) {
                return new RuntimeException("Converted HTTP Exception");
            }

        };
    }

    @Test
    void convert_WhenExceptionIsCleverClientException_ShouldUseItDirectly() {
        // Arrange
        var originalException = new CleverClientException(mockResponseInfo);

        // Act
        var result = exceptionConverter.convert(originalException);

        // Assert
        assertTrue(result instanceof RuntimeException);
        assertEquals("Converted HTTP Exception", result.getMessage());
    }

    @Test
    void convert_WhenExceptionHasCleverClientExceptionCause_ShouldUseTheCause() {
        // Arrange
        var cause = new CleverClientException(mockResponseInfo);
        var originalException = new RuntimeException(cause);

        // Act
        var result = exceptionConverter.convert(originalException);

        // Assert
        assertTrue(result instanceof RuntimeException);
        assertEquals("Converted HTTP Exception", result.getMessage());
    }

    @Test
    void convert_WhenNoResponseInfo_ShouldReturnOriginalException_AsCause() {
        // Arrange
        var originalException = new RuntimeException("Original exception");

        // Act
        var result = exceptionConverter.convert(originalException);

        // Assert
        assertSame(originalException, result.getCause());
        assertEquals("Original exception", result.getCause().getMessage());
    }

    @Test
    void convert_WhenResponseInfoIsEmpty_ShouldReturnOriginalException() {
        // Arrange
        var cleverClientException = new CleverClientException(new RuntimeException("Nested exception"));

        // Act
        var result = exceptionConverter.convert(cleverClientException);

        // Assert
        assertTrue(result instanceof RuntimeException);
        assertEquals("Nested exception", result.getCause().getMessage());
    }

}
