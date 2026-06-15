package com.parsun.parsun_api.reports.movements.application.dto.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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

        var response = new MovementReportResponse(
                movementId, "Main Warehouse", "Product A", "SKU001",
                "Entrada", 10, 5, 15, "Initial stock", now);

        assertEquals(movementId, response.movementId());
        assertEquals("Main Warehouse", response.warehouseName());
        assertEquals("Product A", response.productName());
        assertEquals("SKU001", response.sku());
        assertEquals("Entrada", response.movementType());
        assertEquals(10, response.quantity());
        assertEquals(5, response.previousStock());
        assertEquals(15, response.newStock());
        assertEquals("Initial stock", response.notes());
        assertEquals(now, response.createdAt());
    }

    @Test
    @DisplayName("should create movement report response with null notes")
    void should_create_movement_report_response_with_null_notes() {
        var response = new MovementReportResponse(
                UUID.randomUUID(), "Warehouse", "Product", "SKU",
                "Salida", -5, 10, 5, null, LocalDateTime.now());

        assertNull(response.notes());
    }
}
