package com.cashly.cashly_api.inventory.warehouses.stock.application.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ProductNotInWarehouseException Unit Tests")
class ProductNotInWarehouseExceptionTest {

    @Test
    void should_CreateException_WithProductAndWarehouseIds() {
        UUID productId = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();

        ProductNotInWarehouseException exception =
                new ProductNotInWarehouseException(productId, warehouseId);

        assertTrue(exception.getMessage().contains(productId.toString()));
        assertTrue(exception.getMessage().contains(warehouseId.toString()));
    }

    @Test
    void should_BeRuntimeException() {
        ProductNotInWarehouseException exception =
                new ProductNotInWarehouseException(UUID.randomUUID(), UUID.randomUUID());

        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    void should_NotBeNull() {
        ProductNotInWarehouseException exception =
                new ProductNotInWarehouseException(UUID.randomUUID(), UUID.randomUUID());

        assertNotNull(exception);
    }
}
