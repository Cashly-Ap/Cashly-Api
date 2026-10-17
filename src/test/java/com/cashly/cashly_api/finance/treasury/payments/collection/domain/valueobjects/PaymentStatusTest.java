package com.cashly.cashly_api.finance.treasury.payments.collection.domain.valueobjects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PaymentStatus Enum Unit Tests")
class PaymentStatusTest {

    @Test
    void should_HaveFiveValues() {
        PaymentStatus[] values = PaymentStatus.values();
        assertEquals(5, values.length);
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
    void should_HavePendingValue() {
        assertNotNull(PaymentStatus.valueOf("PENDING"));
        assertEquals(PaymentStatus.PENDING, PaymentStatus.valueOf("PENDING"));
    }

    @Test
    void should_HaveRejectedValue() {
        assertNotNull(PaymentStatus.valueOf("REJECTED"));
        assertEquals(PaymentStatus.REJECTED, PaymentStatus.valueOf("REJECTED"));
    }

    @Test
    void should_BeCorrectlyOrdered() {
        assertSame(PaymentStatus.PENDING, PaymentStatus.values()[0]);
        assertSame(PaymentStatus.APPLIED, PaymentStatus.values()[1]);
        assertSame(PaymentStatus.CANCELLED, PaymentStatus.values()[2]);
        assertSame(PaymentStatus.REJECTED, PaymentStatus.values()[3]);
        assertSame(PaymentStatus.REFUND, PaymentStatus.values()[4]);
    }
}
