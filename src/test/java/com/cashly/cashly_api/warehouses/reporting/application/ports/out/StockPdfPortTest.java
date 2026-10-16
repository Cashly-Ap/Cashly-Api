package com.cashly.cashly_api.warehouses.reporting.application.ports.out;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("StockPdfPort Interface Tests")
class StockPdfPortTest {

    @Test
    @DisplayName("should be accessible as interface")
    void should_be_accessible() {
        assertNotNull(StockPdfPort.class);
    }
}
