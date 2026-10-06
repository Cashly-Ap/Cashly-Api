package com.cashly.cashly_api.reports.stock.application.query;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StockPdfQuery Unit Tests")
class StockPdfQueryTest {

    @Test
    @DisplayName("should create query with all fields")
    void should_create_query_with_all_fields() {
        UUID warehouseId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        var query = new StockPdfQuery(warehouseId, categoryId, true, true);

        assertEquals(warehouseId, query.warehouseId());
        assertEquals(categoryId, query.categoryId());
        assertTrue(query.withStock());
        assertTrue(query.lowStock());
    }

    @Test
    @DisplayName("should create query with null values")
    void should_create_query_with_null_values() {
        var query = new StockPdfQuery(null, null, null, null);

        assertNull(query.warehouseId());
        assertNull(query.categoryId());
        assertNull(query.withStock());
        assertNull(query.lowStock());
    }
}
