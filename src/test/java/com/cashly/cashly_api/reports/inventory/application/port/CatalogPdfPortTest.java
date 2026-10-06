package com.cashly.cashly_api.reports.inventory.application.port;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("CatalogPdfPort Interface Tests")
class CatalogPdfPortTest {

    @Test
    @DisplayName("should be accessible as interface")
    void should_be_accessible() {
        assertNotNull(CatalogPdfPort.class);
    }
}
