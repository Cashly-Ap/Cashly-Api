package com.parsun.parsun_api.payments.domain.valueobjects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PaymentId Value Object Unit Tests")
class PaymentIdUnitTest {

    @Test
    void should_CreatePaymentId_When_ValidUUIDProvided() {
        UUID uuid = UUID.randomUUID();
        PaymentId paymentId = PaymentId.of(uuid);

        assertEquals(uuid, paymentId.value());
    }

    @Test
    void should_ThrowException_When_NullValueProvided() {
        assertThrows(NullPointerException.class, () -> PaymentId.of(null));
    }

    @Test
    void should_ThrowException_When_NullInRecordConstructor() {
        assertThrows(NullPointerException.class, () -> new PaymentId(null));
    }

    @Test
    void should_GenerateRandomId_When_GenerateCalled() {
        PaymentId paymentId = PaymentId.generate();

        assertNotNull(paymentId.value());
    }

    @Test
    void should_GenerateDifferentIds_When_GenerateCalledMultipleTimes() {
        PaymentId first = PaymentId.generate();
        PaymentId second = PaymentId.generate();

        assertNotEquals(first, second);
    }

    @Test
    void should_BeEqual_When_SameValueProvided() {
        UUID uuid = UUID.randomUUID();
        PaymentId first = PaymentId.of(uuid);
        PaymentId second = PaymentId.of(uuid);

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void should_NotBeEqual_When_DifferentValuesProvided() {
        PaymentId first = PaymentId.generate();
        PaymentId second = PaymentId.generate();

        assertNotEquals(first, second);
    }

    @Test
    void should_ReturnUUIDString_When_ToStringCalled() {
        UUID uuid = UUID.randomUUID();
        PaymentId paymentId = PaymentId.of(uuid);

        assertEquals(uuid.toString(), paymentId.toString());
    }
}
