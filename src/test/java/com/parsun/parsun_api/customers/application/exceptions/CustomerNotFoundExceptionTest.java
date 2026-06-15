package com.parsun.parsun_api.customers.application.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CustomerNotFoundException Unit Tests")
class CustomerNotFoundExceptionTest {

    @Test
    void should_CreateException_WithMessage() {
        String message = "Customer not found with id: 123";

        CustomerNotFoundException exception = new CustomerNotFoundException(message);

        assertEquals(message, exception.getMessage());
    }

    @Test
    void should_BeRuntimeException() {
        CustomerNotFoundException exception = new CustomerNotFoundException("test");

        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    void should_AcceptEmptyMessage() {
        CustomerNotFoundException exception = new CustomerNotFoundException("");

        assertEquals("", exception.getMessage());
    }
}
