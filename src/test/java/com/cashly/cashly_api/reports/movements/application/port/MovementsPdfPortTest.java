package com.cashly.cashly_api.reports.movements.application.port;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("MovementsPdfPort Interface Tests")
class MovementsPdfPortTest {

    @Test
    @DisplayName("should be accessible as interface")
    void should_be_accessible() {
        assertNotNull(MovementsPdfPort.class);
    }
}
