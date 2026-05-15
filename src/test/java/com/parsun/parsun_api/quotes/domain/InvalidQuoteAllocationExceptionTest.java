package com.parsun.parsun_api.quotes.domain;

import com.parsun.parsun_api.quotes.domain.exceptions.InvalidQuoteAllocationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InvalidQuoteAllocationExceptionTest {

    @Test
    void should_throw_with_correct_message() {
        String message = "Allocation quantity exceeds line quantity";
        InvalidQuoteAllocationException exception = assertThrows(
                InvalidQuoteAllocationException.class,
                () -> { throw new InvalidQuoteAllocationException(message); }
        );
        assertEquals(message, exception.getMessage());
    }

    @Test
    void should_extend_runtime_exception() {
        InvalidQuoteAllocationException exception = new InvalidQuoteAllocationException("test");
        assertInstanceOf(RuntimeException.class, exception);
    }
}
