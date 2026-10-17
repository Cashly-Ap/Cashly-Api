package com.cashly.cashly_api.inventory.warehouses.reporting.application.dto.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CatalogReportResponse Unit Tests")
class CatalogReportResponseTest {

    @Test
    @DisplayName("should create catalog report response with all fields")
    void should_create_catalog_report_response() {
        UUID productId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();

        var response = new CatalogReportResponse(
                productId, "Product A", "SKU001",
                categoryId, "Electronics", true,
                warehouseId, "Main Warehouse", BigDecimal.valueOf(50), true);

        assertEquals(productId, response.productId());
        assertEquals("Product A", response.productName());
        assertEquals("SKU001", response.sku());
        assertEquals(categoryId, response.categoryId());
        assertEquals("Electronics", response.categoryName());
        assertTrue(response.composite());
        assertEquals(warehouseId, response.warehouseId());
        assertEquals("Main Warehouse", response.warehouseName());
        assertEquals(BigDecimal.valueOf(50), response.quantity());
        assertTrue(response.active());
    }

    @Test
    @DisplayName("should create catalog report response with null values")
    void should_create_catalog_report_response_with_null_values() {
        var response = new CatalogReportResponse(
                UUID.randomUUID(), "Product B", "SKU002",
                UUID.randomUUID(), null, null,
                UUID.randomUUID(), null, null, null);

        assertNull(response.categoryName());
        assertNull(response.composite());
        assertNull(response.warehouseName());
        assertNull(response.quantity());
        assertNull(response.active());
    }
}
