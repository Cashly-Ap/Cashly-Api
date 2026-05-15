package com.parsun.parsun_api.quotes.domain;

import com.parsun.parsun_api.quotes.domain.exceptions.QuoteStateException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QuoteStateExceptionTest {

    @Test
    void should_throw_with_correct_message() {
        String message = "Invalid status transition from DRAFT to ACCEPTED";
        QuoteStateException exception = assertThrows(
                QuoteStateException.class,
                () -> { throw new QuoteStateException(message); }
        );
        assertEquals(message, exception.getMessage());
    }

    @Test
    void should_extend_runtime_exception() {
        QuoteStateException exception = new QuoteStateException("test");
        assertInstanceOf(RuntimeException.class, exception);
    }
}
