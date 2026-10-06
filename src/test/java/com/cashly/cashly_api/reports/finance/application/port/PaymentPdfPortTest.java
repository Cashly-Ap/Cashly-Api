package com.cashly.cashly_api.reports.finance.application.port;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("PaymentPdfPort Interface Tests")
class PaymentPdfPortTest {

    @Test
    @DisplayName("should be accessible as interface")
    void should_be_accessible() {
        assertNotNull(PaymentPdfPort.class);
    }
}
