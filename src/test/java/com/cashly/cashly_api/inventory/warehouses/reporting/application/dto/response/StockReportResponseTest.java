package com.cashly.cashly_api.inventory.warehouses.reporting.application.dto.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StockReportResponse Unit Tests")
class StockReportResponseTest {

    @Test
    @DisplayName("should create stock report response with all fields")
    void should_create_stock_report_response() {
        var response = new StockReportResponse(
                "Main Warehouse", "Product A", "SKU001",
                "Electronics", 50, "Con Stock");

        assertEquals("Main Warehouse", response.warehouseName());
        assertEquals("Product A", response.productName());
        assertEquals("SKU001", response.sku());
        assertEquals("Electronics", response.categoryName());
        assertEquals(50, response.quantity());
        assertEquals("Con Stock", response.status());
    }

    @Test
    @DisplayName("should create stock report response with zero stock")
    void should_create_stock_report_response_with_zero_stock() {
        var response = new StockReportResponse(
                "Warehouse", "Product B", "SKU002",
                "Category", 0, "Sin Stock");

        assertEquals(0, response.quantity());
        assertEquals("Sin Stock", response.status());
    }
}
