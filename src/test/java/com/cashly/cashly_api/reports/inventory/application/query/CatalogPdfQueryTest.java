package com.cashly.cashly_api.reports.inventory.application.query;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CatalogPdfQuery Unit Tests")
class CatalogPdfQueryTest {

    @Test
    @DisplayName("should create query with all fields")
    void should_create_query_with_all_fields() {
        UUID categoryId = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();

        var query = new CatalogPdfQuery(
                "search term", categoryId, warehouseId, true, true, false);

        assertEquals("search term", query.search());
        assertEquals(categoryId, query.categoryId());
        assertEquals(warehouseId, query.warehouseId());
        assertTrue(query.composite());
        assertTrue(query.withStock());
        assertFalse(query.lowStock());
    }

    @Test
    @DisplayName("should create query with null values")
    void should_create_query_with_null_values() {
        var query = new CatalogPdfQuery(null, null, null, null, null, null);

        assertNull(query.search());
        assertNull(query.categoryId());
        assertNull(query.warehouseId());
        assertNull(query.composite());
        assertNull(query.withStock());
        assertNull(query.lowStock());
    }
}
