package com.cashly.cashly_api.inventory.warehouses.reporting.infrastructure.persistence.projections;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("StockReportProjection Interface Tests")
class StockReportProjectionTest {

    @Test
    @DisplayName("should be accessible as interface")
    void should_be_accessible() {
        assertNotNull(StockReportProjection.class);
    }
}
