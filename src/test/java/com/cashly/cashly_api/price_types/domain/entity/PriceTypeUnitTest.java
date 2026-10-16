package com.cashly.cashly_api.price_types.domain.entity;

import com.cashly.cashly_api.price_types.domain.entity.PriceType;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PriceType Domain Entity Unit Tests")
class PriceTypeUnitTest {

    @Test
    void should_CreatePriceType_When_ValidNameProvided() {
        PriceType priceType = PriceType.create("Mayorista", "Precio para mayoristas");

        assertNull(priceType.getId());
        assertEquals("Mayorista", priceType.getName());
        assertEquals("Precio para mayoristas", priceType.getDescription());
        assertTrue(priceType.isActive());
        assertNotNull(priceType.getCreatedAt());
        assertNotNull(priceType.getUpdatedAt());
    }

    @Test
    void should_TrimName_When_CreatingWithWhitespace() {
        PriceType priceType = PriceType.create("  Mayorista  ", null);

        assertEquals("Mayorista", priceType.getName());
    }

    @Test
    void should_AllowNullDescription_When_Creating() {
        PriceType priceType = PriceType.create("Minorista", null);

        assertNull(priceType.getDescription());
    }

    @Test
    void should_ThrowException_When_NameIsNull() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> PriceType.create(null, "desc")
        );
        assertEquals("Price type name cannot be null or blank", exception.getMessage());
    }

    @Test
    void should_ThrowException_When_NameIsBlank() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> PriceType.create("   ", "desc")
        );
        assertEquals("Price type name cannot be null or blank", exception.getMessage());
    }

    @Test
    void should_ThrowException_When_NameExceeds100Characters() {
        String longName = "A".repeat(101);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> PriceType.create(longName, "desc")
        );
        assertEquals("Price type name cannot exceed 100 characters", exception.getMessage());
    }

    @Test
    void should_Accept100CharacterName_When_ExactlyAtLimit() {
        String exactName = "A".repeat(100);

        assertDoesNotThrow(() -> PriceType.create(exactName, null));
    }

    @Test
    void should_Reconstitute_When_AllFieldsProvided() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        PriceType priceType = PriceType.reconstitute(id, "Mayorista", "desc", false, now, now);

        assertEquals(id, priceType.getId());
        assertEquals("Mayorista", priceType.getName());
        assertEquals("desc", priceType.getDescription());
        assertFalse(priceType.isActive());
        assertEquals(now, priceType.getCreatedAt());
        assertEquals(now, priceType.getUpdatedAt());
    }

    @Test
    void should_UpdateFields_When_UpdateCalled() {
        PriceType priceType = PriceType.create("Mayorista", "desc original");

        priceType.update("Minorista", "desc nueva", false);

        assertEquals("Minorista", priceType.getName());
        assertEquals("desc nueva", priceType.getDescription());
        assertFalse(priceType.isActive());
    }

    @Test
    void should_TrimNameOnUpdate_When_WhitespaceProvided() {
        PriceType priceType = PriceType.create("Mayorista", null);

        priceType.update("  Minorista  ", null, null);

        assertEquals("Minorista", priceType.getName());
    }

    @Test
    void should_NotChangeActive_When_UpdateCalledWithNullActive() {
        PriceType priceType = PriceType.create("Mayorista", null);
        assertTrue(priceType.isActive());

        priceType.update("Mayorista", null, null);

        assertTrue(priceType.isActive());
    }

    @Test
    void should_UpdateUpdatedAt_When_UpdateCalled() throws InterruptedException {
        PriceType priceType = PriceType.create("Mayorista", null);
        LocalDateTime before = priceType.getUpdatedAt();

        Thread.sleep(10);
        priceType.update("Minorista", null, true);

        assertTrue(priceType.getUpdatedAt().isAfter(before));
    }

    @Test
    void should_ThrowException_When_UpdateWithNullName() {
        PriceType priceType = PriceType.create("Mayorista", null);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> priceType.update(null, null, null)
        );
        assertEquals("Price type name cannot be null or blank", exception.getMessage());
    }

    @Test
    void should_BeEqual_When_SameIdProvided() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        PriceType pt1 = PriceType.reconstitute(id, "Mayorista", null, true, now, now);
        PriceType pt2 = PriceType.reconstitute(id, "Minorista", "desc", false, now, now);

        assertEquals(pt1, pt2);
        assertEquals(pt1.hashCode(), pt2.hashCode());
    }

    @Test
    void should_NotBeEqual_When_DifferentIdsProvided() {
        LocalDateTime now = LocalDateTime.now();
        PriceType pt1 = PriceType.reconstitute(UUID.randomUUID(), "Mayorista", null, true, now, now);
        PriceType pt2 = PriceType.reconstitute(UUID.randomUUID(), "Mayorista", null, true, now, now);

        assertNotEquals(pt1, pt2);
    }
}
