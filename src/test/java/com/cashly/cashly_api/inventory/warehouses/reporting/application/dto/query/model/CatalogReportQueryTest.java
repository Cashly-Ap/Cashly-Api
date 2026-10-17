package com.cashly.cashly_api.inventory.warehouses.reporting.application.dto.query.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CatalogReportQuery Unit Tests")
class CatalogReportQueryTest {

    @Test
    @DisplayName("should create query with all fields")
    void should_create_query_with_all_fields() {
        UUID categoryId = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();

        var query = new CatalogReportQuery(
                "search", categoryId, warehouseId, true, false, true, 0, 50);

        assertEquals("search", query.search());
        assertEquals(categoryId, query.categoryId());
        assertEquals(warehouseId, query.warehouseId());
        assertTrue(query.composite());
        assertFalse(query.withStock());
        assertTrue(query.lowStock());
        assertEquals(0, query.page());
        assertEquals(50, query.size());
    }

    @Test
    @DisplayName("should create query with default page values")
    void should_create_query_with_default_page_values() {
        var query = new CatalogReportQuery(
                null, null, null, null, null, null, 0, 20);

        assertEquals(0, query.page());
        assertEquals(20, query.size());
        assertNull(query.search());
    }
}
