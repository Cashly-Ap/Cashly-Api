package com.cashly.cashly_api.warehouses.reporting.infrastructure.persistence.projections;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("MovementsReportProjection Interface Tests")
class MovementsReportProjectionTest {

    @Test
    @DisplayName("should be accessible as interface")
    void should_be_accessible() {
        assertNotNull(MovementsReportProjection.class);
    }
}
