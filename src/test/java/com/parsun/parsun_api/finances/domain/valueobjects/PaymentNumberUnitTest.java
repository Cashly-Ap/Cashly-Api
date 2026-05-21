package com.parsun.parsun_api.finances.domain.valueobjects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PaymentNumber Value Object Unit Tests")
class PaymentNumberUnitTest {

    @Test
    void should_FormatPaymentNumber_Correctly() {
        PaymentNumber number = PaymentNumber.of(2025, 42);

        assertEquals("PAY-2025-0042", number.value());
    }

    @Test
    void should_FormatSingleDigitSequence() {
        PaymentNumber number = PaymentNumber.of(2025, 1);

        assertEquals("PAY-2025-0001", number.value());
    }

    @Test
    void should_FormatLargeSequence() {
        PaymentNumber number = PaymentNumber.of(2025, 9999);

        assertEquals("PAY-2025-9999", number.value());
    }

    @Test
    void should_ThrowException_When_SequenceIsZeroOrLess() {
        assertThrows(IllegalArgumentException.class, () -> PaymentNumber.of(2025, 0));
        assertThrows(IllegalArgumentException.class, () -> PaymentNumber.of(2025, -1));
    }

    @Test
    void should_BeEqual_When_SameValue() {
        PaymentNumber n1 = PaymentNumber.of(2025, 42);
        PaymentNumber n2 = PaymentNumber.of(2025, 42);

        assertEquals(n1, n2);
        assertEquals(n1.hashCode(), n2.hashCode());
    }

    @Test
    void should_NotBeEqual_When_DifferentValues() {
        PaymentNumber n1 = PaymentNumber.of(2025, 42);
        PaymentNumber n2 = PaymentNumber.of(2025, 43);

        assertNotEquals(n1, n2);
    }
}
