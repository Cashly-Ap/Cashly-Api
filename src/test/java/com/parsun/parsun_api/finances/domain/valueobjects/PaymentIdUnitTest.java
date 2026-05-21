package com.parsun.parsun_api.finances.domain.valueobjects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PaymentId Value Object Unit Tests")
class PaymentIdUnitTest {

    @Test
    void should_CreatePaymentId_With_ValidUuid() {
        UUID uuid = UUID.randomUUID();
        PaymentId id = PaymentId.of(uuid);

        assertEquals(uuid, id.value());
    }

    @Test
    void should_GenerateNewPaymentId() {
        PaymentId id = PaymentId.generate();

        assertNotNull(id.value());
    }

    @Test
    void should_ThrowException_When_UuidIsNull() {
        assertThrows(NullPointerException.class, () -> PaymentId.of(null));
    }

    @Test
    void should_BeEqual_When_SameUuid() {
        UUID uuid = UUID.randomUUID();
        PaymentId id1 = PaymentId.of(uuid);
        PaymentId id2 = PaymentId.of(uuid);

        assertEquals(id1, id2);
        assertEquals(id1.hashCode(), id2.hashCode());
    }

    @Test
    void should_NotBeEqual_When_DifferentUuids() {
        PaymentId id1 = PaymentId.generate();
        PaymentId id2 = PaymentId.generate();

        assertNotEquals(id1, id2);
    }
}
