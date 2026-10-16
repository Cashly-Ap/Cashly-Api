package com.cashly.cashly_api.warehouses.management.application.create;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DuplicateWarehouseCodeException Unit Tests")
class DuplicateWarehouseCodeExceptionTest {

    @Test
    void should_CreateException_WithCodeInMessage() {
        String code = "WH-001";

        DuplicateWarehouseCodeException exception = new DuplicateWarehouseCodeException(code);

        assertTrue(exception.getMessage().contains(code));
        assertTrue(exception.getMessage().contains("already exists"));
    }

    @Test
    void should_BeRuntimeException() {
        DuplicateWarehouseCodeException exception = new DuplicateWarehouseCodeException("TEST");

        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    void should_NotBeNull() {
        DuplicateWarehouseCodeException exception = new DuplicateWarehouseCodeException("WH-002");

        assertNotNull(exception);
    }
}
