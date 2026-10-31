package com.cashly.cashly_api.inventory.warehouses.reporting.application.dto.query.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MovementsPdfQuery Unit Tests")
class MovementsPdfQueryTest {

    @Test
    @DisplayName("should create query with all fields")
    void should_create_query_with_all_fields() {
        UUID warehouseId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        LocalDate dateFrom = LocalDate.of(2026, 1, 1);
        LocalDate dateTo = LocalDate.of(2026, 12, 31);

        var query = new MovementsPdfQuery(warehouseId, productId, "ENTRY",
                dateFrom, dateTo);

        assertEquals(warehouseId, query.warehouseId());
        assertEquals(productId, query.productId());
        assertEquals("ENTRY", query.movementType());
        assertEquals(dateFrom, query.dateFrom());
        assertEquals(dateTo, query.dateTo());
    }

    @Test
    @DisplayName("should create query with null values")
    void should_create_query_with_null_values() {
        var query = new MovementsPdfQuery(null, null, null, null, null);

        assertNull(query.warehouseId());
        assertNull(query.productId());
        assertNull(query.movementType());
        assertNull(query.dateFrom());
        assertNull(query.dateTo());
    }
}
