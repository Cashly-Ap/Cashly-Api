package com.cashly.cashly_api.inventory.warehouses.reporting.application.dto.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MovementReportResponse Unit Tests")
class MovementReportResponseTest {

    @Test
    @DisplayName("should create movement report response with all fields")
    void should_create_movement_report_response() {
        UUID movementId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        var response = new MovementReportResponse(movementId, "Main Warehouse",
                "Product A", "SKU001", "Entrada", new BigDecimal("10"), new BigDecimal("5"),
                new BigDecimal("15"), "Initial stock",
                now);

        assertEquals(movementId, response.movementId());
        assertEquals("Main Warehouse", response.warehouseName());
        assertEquals("Product A", response.productName());
        assertEquals("SKU001", response.sku());
        assertEquals("Entrada", response.movementType());
        assertEquals(0, new BigDecimal("10").compareTo(response.quantity()));
        assertEquals(0, new BigDecimal("5").compareTo(response.previousStock()));
        assertEquals(0, new BigDecimal("15").compareTo(response.newStock()));
        assertEquals("Initial stock", response.notes());
        assertEquals(now, response.createdAt());
    }

    @Test
    @DisplayName("should create movement report response with null notes")
    void should_create_movement_report_response_with_null_notes() {
        var response = new MovementReportResponse(UUID.randomUUID(),
                "Warehouse", "Product", "SKU", "Salida", new BigDecimal("-5"),
                new BigDecimal("10"), new BigDecimal("5"), null,
                LocalDateTime.now());

        assertNull(response.notes());
    }
}
