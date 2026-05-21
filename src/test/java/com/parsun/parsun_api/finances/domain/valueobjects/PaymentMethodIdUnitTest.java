package com.parsun.parsun_api.finances.domain.valueobjects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PaymentMethodId Value Object Unit Tests")
class PaymentMethodIdUnitTest {

    @Test
    void should_CreatePaymentMethodId_With_ValidUuid() {
        UUID uuid = UUID.randomUUID();
        PaymentMethodId id = PaymentMethodId.of(uuid);

        assertEquals(uuid, id.value());
    }

    @Test
    void should_GenerateNewPaymentMethodId() {
        PaymentMethodId id = PaymentMethodId.generate();

        assertNotNull(id.value());
    }

    @Test
    void should_ThrowException_When_UuidIsNull() {
        assertThrows(NullPointerException.class, () -> PaymentMethodId.of(null));
    }

    @Test
    void should_BeEqual_When_SameUuid() {
        UUID uuid = UUID.randomUUID();
        PaymentMethodId id1 = PaymentMethodId.of(uuid);
        PaymentMethodId id2 = PaymentMethodId.of(uuid);

        assertEquals(id1, id2);
        assertEquals(id1.hashCode(), id2.hashCode());
    }

    @Test
    void should_NotBeEqual_When_DifferentUuids() {
        PaymentMethodId id1 = PaymentMethodId.generate();
        PaymentMethodId id2 = PaymentMethodId.generate();

        assertNotEquals(id1, id2);
    }
}
