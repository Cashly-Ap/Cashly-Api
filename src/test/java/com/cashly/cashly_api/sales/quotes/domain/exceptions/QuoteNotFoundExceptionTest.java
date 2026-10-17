package com.cashly.cashly_api.sales.quotes.domain.exceptions;

import com.cashly.cashly_api.sales.quotes.domain.exceptions.QuoteNotFoundException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class QuoteNotFoundExceptionTest {

    @Test
    void should_throw_with_correct_message() {
        UUID id = UUID.randomUUID();
        QuoteNotFoundException exception = assertThrows(
                QuoteNotFoundException.class,
                () -> { throw new QuoteNotFoundException(id); }
        );
        assertTrue(exception.getMessage().contains(id.toString()));
    }

    @Test
    void should_extend_runtime_exception() {
        UUID id = UUID.randomUUID();
        QuoteNotFoundException exception = new QuoteNotFoundException(id);
        assertInstanceOf(RuntimeException.class, exception);
    }
}
