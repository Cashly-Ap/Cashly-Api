package com.cashly.cashly_api.sales.orders.domain.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OrderNotFoundException Unit Tests")
class OrderNotFoundExceptionTest {

    @Test
    void should_CreateException_WithIdInMessage() {
        UUID id = UUID.randomUUID();

        OrderNotFoundException exception = new OrderNotFoundException(id);

        assertTrue(exception.getMessage().contains(id.toString()));
        assertTrue(exception.getMessage().contains("Order not found"));
    }

    @Test
    void should_BeRuntimeException() {
        OrderNotFoundException exception = new OrderNotFoundException(
                UUID.randomUUID());

        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    void should_NotBeNull() {
        OrderNotFoundException exception = new OrderNotFoundException(
                UUID.randomUUID());

        assertNotNull(exception);
    }
}
