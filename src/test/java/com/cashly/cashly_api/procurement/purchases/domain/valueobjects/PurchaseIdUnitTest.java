package com.cashly.cashly_api.procurement.purchases.domain.valueobjects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PurchaseId Value Object Unit Tests")
class PurchaseIdUnitTest {

    @Test
    void should_CreatePurchaseId_When_ValidUuidProvided() {
        UUID uuid = UUID.randomUUID();
        PurchaseId purchaseId = PurchaseId.of(uuid);

        assertEquals(uuid, purchaseId.value());
    }

    @Test
    void should_GenerateUniqueId_When_GenerateCalled() {
        PurchaseId first = PurchaseId.generate();
        PurchaseId second = PurchaseId.generate();

        assertNotEquals(first, second);
    }

    @Test
    void should_GenerateNonNullId() {
        PurchaseId purchaseId = PurchaseId.generate();

        assertNotNull(purchaseId);
        assertNotNull(purchaseId.value());
    }

    @Test
    void should_ThrowException_When_NullValueProvided() {
        assertThrows(NullPointerException.class, () -> new PurchaseId(null));
    }

    @Test
    void should_BeEqual_When_SameUuidProvided() {
        UUID uuid = UUID.randomUUID();
        PurchaseId first = PurchaseId.of(uuid);
        PurchaseId second = PurchaseId.of(uuid);

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void should_NotBeEqual_When_DifferentUuidsProvided() {
        PurchaseId first = PurchaseId.generate();
        PurchaseId second = PurchaseId.generate();

        assertNotEquals(first, second);
    }

    @Test
    void should_HaveConsistentHashCode() {
        UUID uuid = UUID.randomUUID();
        PurchaseId purchaseId = PurchaseId.of(uuid);

        assertEquals(uuid.hashCode(), purchaseId.hashCode());
    }
}
