package com.cashly.cashly_api.finance.treasury.payments.collection.domain.valueobjects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PaymentNumber Value Object Unit Tests")
class PaymentNumberUnitTest {

    @Test
    void should_CreatePaymentNumber_When_ValidYearAndSequenceProvided() {
        PaymentNumber paymentNumber = PaymentNumber.of(2026, 1);

        assertEquals("PAY-2026-0001", paymentNumber.value());
    }

    @Test
    void should_PadSequence_When_SequenceHasLessThan4Digits() {
        PaymentNumber paymentNumber = PaymentNumber.of(2026, 42);

        assertEquals("PAY-2026-0042", paymentNumber.value());
    }

    @Test
    void should_CreatePaymentNumber_When_DirectValueProvided() {
        PaymentNumber paymentNumber = new PaymentNumber("PAY-2026-0001");

        assertEquals("PAY-2026-0001", paymentNumber.value());
    }

    @Test
    void should_ThrowException_When_NullValueProvided() {
        assertThrows(NullPointerException.class, () -> new PaymentNumber(null));
    }

    @Test
    void should_ThrowException_When_BlankValueProvided() {
        assertThrows(IllegalArgumentException.class,
                () -> new PaymentNumber("   "));
    }

    @Test
    void should_ThrowException_When_SequenceIsZero() {
        assertThrows(IllegalArgumentException.class,
                () -> PaymentNumber.of(2026, 0));
    }

    @Test
    void should_ThrowException_When_SequenceIsNegative() {
        assertThrows(IllegalArgumentException.class,
                () -> PaymentNumber.of(2026, -1));
    }

    @Test
    void should_BeEqual_When_SameValueProvided() {
        PaymentNumber first = new PaymentNumber("PAY-2026-0001");
        PaymentNumber second = new PaymentNumber("PAY-2026-0001");

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void should_NotBeEqual_When_DifferentValuesProvided() {
        PaymentNumber first = PaymentNumber.of(2026, 1);
        PaymentNumber second = PaymentNumber.of(2026, 2);

        assertNotEquals(first, second);
    }

    @Test
    void should_ReturnValue_When_ToStringCalled() {
        PaymentNumber paymentNumber = PaymentNumber.of(2026, 1);

        assertEquals("PaymentNumber[value=PAY-2026-0001]",
                paymentNumber.toString());
    }
}
