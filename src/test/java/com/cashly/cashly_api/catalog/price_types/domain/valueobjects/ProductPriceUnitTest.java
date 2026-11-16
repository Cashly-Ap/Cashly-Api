package com.cashly.cashly_api.catalog.price_types.domain.valueobjects;

import com.cashly.cashly_api.catalog.products.domain.valueobjects.ProductPrice;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ProductPrice Value Object Unit Tests")
class ProductPriceUnitTest {

    @Test
    void should_Create_When_ValidArguments() {
        UUID priceTypeId = UUID.randomUUID();

        ProductPrice pp = ProductPrice.create(priceTypeId,
                new BigDecimal("99.99"), null);

        assertNull(pp.getId());
        assertEquals(priceTypeId, pp.getPriceTypeId());
        assertEquals(new BigDecimal("99.99"), pp.getPrice());
        assertNull(pp.getProfitMargin());
    }

    @Test
    void should_Reconstitute_When_AllFieldsProvided() {
        UUID id = UUID.randomUUID();
        UUID priceTypeId = UUID.randomUUID();

        ProductPrice pp = ProductPrice.reconstitute(id, priceTypeId,
                new BigDecimal("50.00"), null);

        assertEquals(id, pp.getId());
        assertEquals(priceTypeId, pp.getPriceTypeId());
        assertEquals(new BigDecimal("50.00"), pp.getPrice());
        assertNull(pp.getProfitMargin());
    }

    @Test
    void should_ThrowException_When_PriceTypeIdIsNull() {
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> ProductPrice.create(null, new BigDecimal("10.00"), null));
        assertEquals("Price type ID cannot be null", exception.getMessage());
    }

    @Test
    void should_ThrowException_When_PriceIsNull() {
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> ProductPrice.create(UUID.randomUUID(), null, null));
        assertEquals("Price cannot be null", exception.getMessage());
    }

    @Test
    void should_ThrowException_When_PriceIsNegative() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ProductPrice.create(UUID.randomUUID(),
                        new BigDecimal("-1.00"), null));
        assertEquals("Price cannot be negative", exception.getMessage());
    }

    @Test
    void should_AllowZeroPrice() {
        assertDoesNotThrow(() -> ProductPrice.create(UUID.randomUUID(),
                BigDecimal.ZERO, null));
    }

    @Test
    void should_BeEqual_When_SamePriceTypeId() {
        UUID priceTypeId = UUID.randomUUID();
        ProductPrice pp1 = ProductPrice.create(priceTypeId,
                new BigDecimal("10.00"), null);
        ProductPrice pp2 = ProductPrice.create(priceTypeId,
                new BigDecimal("99.99"), null);

        assertEquals(pp1, pp2);
        assertEquals(pp1.hashCode(), pp2.hashCode());
    }

    @Test
    void should_NotBeEqual_When_DifferentPriceTypeIds() {
        ProductPrice pp1 = ProductPrice.create(UUID.randomUUID(),
                new BigDecimal("10.00"), null);
        ProductPrice pp2 = ProductPrice.create(UUID.randomUUID(),
                new BigDecimal("10.00"), null);

        assertNotEquals(pp1, pp2);
    }
}
