package com.cashly.cashly_api.partners.customers.warehouse_access.application.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CustomerWarehouseAccessAlreadyExistsException Unit Tests")
class CustomerWarehouseAccessAlreadyExistsExceptionTest {

    @Test
    void should_CreateException_WithMessageContainingIds() {
        UUID customerId = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();

        CustomerWarehouseAccessAlreadyExistsException exception =
                new CustomerWarehouseAccessAlreadyExistsException(customerId, warehouseId);

        assertTrue(exception.getMessage().contains(customerId.toString()));
        assertTrue(exception.getMessage().contains(warehouseId.toString()));
    }

    @Test
    void should_BeRuntimeException() {
        CustomerWarehouseAccessAlreadyExistsException exception =
                new CustomerWarehouseAccessAlreadyExistsException(UUID.randomUUID(), UUID.randomUUID());

        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    void should_NotBeNull() {
        CustomerWarehouseAccessAlreadyExistsException exception =
                new CustomerWarehouseAccessAlreadyExistsException(UUID.randomUUID(), UUID.randomUUID());

        assertNotNull(exception);
    }
}
