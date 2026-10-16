package com.cashly.cashly_api.quotes.domain.exceptions;

import com.cashly.cashly_api.quotes.domain.exceptions.InvalidQuoteLineException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InvalidQuoteLineExceptionTest {

    @Test
    void should_throw_with_correct_message() {
        String message = "Line quantity must be greater than zero";
        InvalidQuoteLineException exception = assertThrows(
                InvalidQuoteLineException.class,
                () -> { throw new InvalidQuoteLineException(message); }
        );
        assertEquals(message, exception.getMessage());
    }

    @Test
    void should_extend_runtime_exception() {
        InvalidQuoteLineException exception = new InvalidQuoteLineException("test");
        assertInstanceOf(RuntimeException.class, exception);
    }
}
