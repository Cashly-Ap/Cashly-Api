package com.parsun.parsun_api.payments.domain.valueobjects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PaymentStatus Enum Unit Tests")
class PaymentStatusTest {

    @Test
    void should_HaveThreeValues() {
        PaymentStatus[] values = PaymentStatus.values();
        assertEquals(3, values.length);
    }

    @Test
    void should_HaveAppliedValue() {
        assertNotNull(PaymentStatus.valueOf("APPLIED"));
        assertEquals(PaymentStatus.APPLIED, PaymentStatus.valueOf("APPLIED"));
    }

    @Test
    void should_HaveCancelledValue() {
        assertNotNull(PaymentStatus.valueOf("CANCELLED"));
        assertEquals(PaymentStatus.CANCELLED, PaymentStatus.valueOf("CANCELLED"));
    }

    @Test
    void should_HaveRefundValue() {
        assertNotNull(PaymentStatus.valueOf("REFUND"));
        assertEquals(PaymentStatus.REFUND, PaymentStatus.valueOf("REFUND"));
    }

    @Test
    void should_BeCorrectlyOrdered() {
        assertSame(PaymentStatus.APPLIED, PaymentStatus.values()[0]);
        assertSame(PaymentStatus.CANCELLED, PaymentStatus.values()[1]);
        assertSame(PaymentStatus.REFUND, PaymentStatus.values()[2]);
    }
}
