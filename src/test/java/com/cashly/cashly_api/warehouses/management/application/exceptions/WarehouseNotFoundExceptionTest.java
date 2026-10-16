package com.cashly.cashly_api.warehouses.management.application.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("WarehouseNotFoundException Unit Tests")
class WarehouseNotFoundExceptionTest {

    @Test
    @DisplayName("should create exception with correct message")
    void should_create_exception_with_correct_message() {
        UUID id = UUID.randomUUID();

        WarehouseNotFoundException exception = new WarehouseNotFoundException(id);

        assertEquals("Warehouse not found with id: " + id, exception.getMessage());
    }

    @Test
    @DisplayName("should be runtime exception")
    void should_be_runtime_exception() {
        WarehouseNotFoundException exception = new WarehouseNotFoundException(UUID.randomUUID());

        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    @DisplayName("should preserve id in message when null")
    void should_preserve_id_in_message_when_null() {
        WarehouseNotFoundException exception = new WarehouseNotFoundException(null);

        assertEquals("Warehouse not found with id: null", exception.getMessage());
    }
}
