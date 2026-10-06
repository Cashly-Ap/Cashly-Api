package com.cashly.cashly_api.payment_methods.domain.valueobjects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PaymentMethodId Value Object Unit Tests")
class PaymentMethodIdUnitTest {

    @Test
    void should_Create_When_ValidUUID() {
        UUID uuid = UUID.randomUUID();
        PaymentMethodId id = PaymentMethodId.of(uuid);

        assertEquals(uuid, id.value());
    }

    @Test
    void should_GenerateUniqueIds_When_Called() {
        PaymentMethodId id1 = PaymentMethodId.generate();
        PaymentMethodId id2 = PaymentMethodId.generate();

        assertNotNull(id1.value());
        assertNotNull(id2.value());
        assertNotEquals(id1, id2);
    }

    @Test
    void should_BeEqual_When_SameValue() {
        UUID uuid = UUID.randomUUID();
        PaymentMethodId id1 = PaymentMethodId.of(uuid);
        PaymentMethodId id2 = PaymentMethodId.of(uuid);

        assertEquals(id1, id2);
        assertEquals(id1.hashCode(), id2.hashCode());
    }

    @Test
    void should_NotBeEqual_When_DifferentValues() {
        PaymentMethodId id1 = PaymentMethodId.generate();
        PaymentMethodId id2 = PaymentMethodId.generate();

        assertNotEquals(id1, id2);
        assertNotEquals(id1.hashCode(), id2.hashCode());
    }

    @Test
    void toString_Should_ContainUUID() {
        UUID uuid = UUID.randomUUID();
        PaymentMethodId id = PaymentMethodId.of(uuid);

        assertTrue(id.toString().contains(uuid.toString()));
    }

    @Test
    void should_ThrowException_When_ValueIsNull() {
        assertThrows(NullPointerException.class, () -> PaymentMethodId.of(null));
    }
}
