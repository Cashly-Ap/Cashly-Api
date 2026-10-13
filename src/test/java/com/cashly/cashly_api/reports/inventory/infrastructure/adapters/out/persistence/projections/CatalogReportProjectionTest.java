package com.cashly.cashly_api.reports.inventory.infrastructure.adapters.out.persistence.projections;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("CatalogReportProjection Interface Tests")
class CatalogReportProjectionTest {

    @Test
    @DisplayName("should be accessible as interface")
    void should_be_accessible() {
        assertNotNull(CatalogReportProjection.class);
    }
}
