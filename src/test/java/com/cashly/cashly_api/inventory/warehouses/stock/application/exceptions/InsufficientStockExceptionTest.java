package com.cashly.cashly_api.inventory.warehouses.stock.application.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("InsufficientStockException Unit Tests")
class InsufficientStockExceptionTest {

    @Test
    @DisplayName("should create exception with correct message")
    void should_create_exception_with_correct_message() {
        UUID productId = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();

        InsufficientStockException exception = new InsufficientStockException(
                productId, warehouseId, 5, 10);

        String message = exception.getMessage();
        assertTrue(message.contains(productId.toString()));
        assertTrue(message.contains(warehouseId.toString()));
        assertTrue(message.contains("available=5"));
        assertTrue(message.contains("requested=10"));
    }

    @Test
    @DisplayName("should be runtime exception")
    void should_be_runtime_exception() {
        InsufficientStockException exception = new InsufficientStockException(
                UUID.randomUUID(), UUID.randomUUID(), 0, 1);

        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    @DisplayName("should format message with available and requested quantities")
    void should_format_message_with_available_and_requested_quantities() {
        InsufficientStockException exception = new InsufficientStockException(
                UUID.randomUUID(), UUID.randomUUID(), 3, 20);

        assertTrue(exception.getMessage()
                .startsWith("Insufficient stock for product "));
        assertTrue(exception.getMessage().contains("available=3"));
        assertTrue(exception.getMessage().contains("requested=20"));
    }
}
