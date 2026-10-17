package com.cashly.cashly_api.inventory.warehouses.management.application.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("WarehouseNotActiveException Unit Tests")
class WarehouseNotActiveExceptionTest {

    @Test
    @DisplayName("should create exception with correct message")
    void should_create_exception_with_correct_message() {
        UUID id = UUID.randomUUID();

        WarehouseNotActiveException exception = new WarehouseNotActiveException(id);

        assertEquals("Warehouse is not active: " + id, exception.getMessage());
    }

    @Test
    @DisplayName("should be runtime exception")
    void should_be_runtime_exception() {
        WarehouseNotActiveException exception = new WarehouseNotActiveException(UUID.randomUUID());

        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    @DisplayName("should preserve id in message when null")
    void should_preserve_id_in_message_when_null() {
        WarehouseNotActiveException exception = new WarehouseNotActiveException(null);

        assertEquals("Warehouse is not active: null", exception.getMessage());
    }
}
