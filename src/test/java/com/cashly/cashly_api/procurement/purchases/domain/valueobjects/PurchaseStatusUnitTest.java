package com.cashly.cashly_api.procurement.purchases.domain.valueobjects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PurchaseStatus Value Object Unit Tests")
class PurchaseStatusUnitTest {

    @Test
    void should_HaveAllExpectedEnumValues() {
        PurchaseStatus[] values = PurchaseStatus.values();

        assertEquals(3, values.length);
        assertNotNull(PurchaseStatus.valueOf("DRAFT"));
        assertNotNull(PurchaseStatus.valueOf("CONFIRMED"));
        assertNotNull(PurchaseStatus.valueOf("CANCELLED"));
    }

    @Test
    void should_ReturnDraft_When_ValueOfDraft() {
        assertEquals(PurchaseStatus.DRAFT, PurchaseStatus.valueOf("DRAFT"));
    }

    @Test
    void should_ReturnConfirmed_When_ValueOfConfirmed() {
        assertEquals(PurchaseStatus.CONFIRMED, PurchaseStatus.valueOf("CONFIRMED"));
    }

    @Test
    void should_ReturnCancelled_When_ValueOfCancelled() {
        assertEquals(PurchaseStatus.CANCELLED, PurchaseStatus.valueOf("CANCELLED"));
    }

    @Test
    void should_ThrowException_When_InvalidStatusName() {
        assertThrows(IllegalArgumentException.class, () -> PurchaseStatus.valueOf("INVALID"));
    }
}
