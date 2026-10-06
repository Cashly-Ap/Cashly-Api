package com.cashly.cashly_api.invoices.domain.valueobjects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PaymentType Value Object Unit Tests")
class PaymentTypeUnitTest {

    @Test
    void should_HaveAllExpectedEnumValues() {
        PaymentType[] values = PaymentType.values();

        assertEquals(2, values.length);
        assertNotNull(PaymentType.valueOf("CASH"));
        assertNotNull(PaymentType.valueOf("CREDIT"));
    }

    @Test
    void should_ReturnCash_When_ValueOfCash() {
        assertEquals(PaymentType.CASH, PaymentType.valueOf("CASH"));
    }

    @Test
    void should_ReturnCredit_When_ValueOfCredit() {
        assertEquals(PaymentType.CREDIT, PaymentType.valueOf("CREDIT"));
    }

    @Test
    void should_ThrowException_When_InvalidPaymentTypeName() {
        assertThrows(IllegalArgumentException.class, () -> PaymentType.valueOf("INVALID"));
    }
}
