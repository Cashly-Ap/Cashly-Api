package com.cashly.cashly_api.procurement.purchases.domain.entity;

import com.cashly.cashly_api.procurement.purchases.domain.valueobjects.PurchaseLineId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PurchaseLine Domain Entity Unit Tests")
class PurchaseLineUnitTest {

    private static final UUID PRODUCT_ID = UUID.randomUUID();
    private static final String PRODUCT_NAME = "Producto A";
    private static final String PRODUCT_CODE = "PROD-001";

    @Test
    void should_CreatePurchaseLine_When_ValidDataProvided() {
        PurchaseLine line = PurchaseLine.create(PRODUCT_ID, PRODUCT_NAME, PRODUCT_CODE,
                new BigDecimal("2"), new BigDecimal("10.00"), List.of());

        assertNotNull(line.getId());
        assertEquals(PRODUCT_ID, line.getProductId());
        assertEquals(PRODUCT_NAME, line.getProductName());
        assertEquals(PRODUCT_CODE, line.getProductCode());
        assertEquals(new BigDecimal("2"), line.getQuantity());
        assertEquals(new BigDecimal("10.00"), line.getUnitCost());
        assertEquals(new BigDecimal("20.00"), line.getLineTotal());
        assertTrue(line.getAllocations().isEmpty());
    }

    @Test
    void should_CalculateLineTotal_When_Created() {
        PurchaseLine line = PurchaseLine.create(PRODUCT_ID, PRODUCT_NAME, PRODUCT_CODE,
                new BigDecimal("3"), new BigDecimal("5.50"), List.of());

        assertEquals(new BigDecimal("16.50"), line.getLineTotal());
    }

    @Test
    void should_CalculateLineTotal_WithDecimalPrecision() {
        PurchaseLine line = PurchaseLine.create(PRODUCT_ID, PRODUCT_NAME, PRODUCT_CODE,
                new BigDecimal("1.5"), new BigDecimal("3.33"), List.of());

        assertEquals(new BigDecimal("4.995"), line.getLineTotal());
    }

    @Test
    void should_CreateWithAllocations_When_Provided() {
        PurchaseLineAllocation allocation = PurchaseLineAllocation.create(
                UUID.randomUUID(), BigDecimal.valueOf(5));

        PurchaseLine line = PurchaseLine.create(PRODUCT_ID, PRODUCT_NAME, PRODUCT_CODE,
                BigDecimal.ONE, BigDecimal.TEN, List.of(allocation));

        assertEquals(1, line.getAllocations().size());
        assertEquals(allocation.getWarehouseId(), line.getAllocations().get(0).getWarehouseId());
    }

    @Test
    void should_CreateWithEmptyAllocations_When_NullProvided() {
        PurchaseLine line = PurchaseLine.create(PRODUCT_ID, PRODUCT_NAME, PRODUCT_CODE,
                BigDecimal.ONE, BigDecimal.TEN, null);

        assertNotNull(line.getAllocations());
        assertTrue(line.getAllocations().isEmpty());
    }

    @Test
    void should_ThrowException_When_IdIsNull() {
        assertThrows(NullPointerException.class, () ->
                PurchaseLine.reconstitute(null, PRODUCT_ID, PRODUCT_NAME, PRODUCT_CODE,
                        BigDecimal.ONE, BigDecimal.TEN, BigDecimal.ZERO, List.of()));
    }

    @Test
    void should_ThrowException_When_ProductIdIsNull() {
        assertThrows(NullPointerException.class, () ->
                PurchaseLine.create(null, PRODUCT_NAME, PRODUCT_CODE,
                        BigDecimal.ONE, BigDecimal.TEN, List.of()));
    }

    @Test
    void should_ThrowException_When_ProductNameIsNull() {
        assertThrows(NullPointerException.class, () ->
                PurchaseLine.create(PRODUCT_ID, null, PRODUCT_CODE,
                        BigDecimal.ONE, BigDecimal.TEN, List.of()));
    }

    @Test
    void should_ThrowException_When_ProductCodeIsNull() {
        assertThrows(NullPointerException.class, () ->
                PurchaseLine.create(PRODUCT_ID, PRODUCT_NAME, null,
                        BigDecimal.ONE, BigDecimal.TEN, List.of()));
    }

    @Test
    void should_ThrowException_When_QuantityIsNull() {
        assertThrows(NullPointerException.class, () ->
                PurchaseLine.create(PRODUCT_ID, PRODUCT_NAME, PRODUCT_CODE,
                        null, BigDecimal.TEN, List.of()));
    }

    @Test
    void should_ThrowException_When_UnitCostIsNull() {
        assertThrows(NullPointerException.class, () ->
                PurchaseLine.create(PRODUCT_ID, PRODUCT_NAME, PRODUCT_CODE,
                        BigDecimal.ONE, null, List.of()));
    }

    @Test
    void should_ThrowException_When_QuantityIsZero() {
        assertThrows(IllegalArgumentException.class, () ->
                PurchaseLine.create(PRODUCT_ID, PRODUCT_NAME, PRODUCT_CODE,
                        BigDecimal.ZERO, BigDecimal.TEN, List.of()));
    }

    @Test
    void should_ThrowException_When_QuantityIsNegative() {
        assertThrows(IllegalArgumentException.class, () ->
                PurchaseLine.create(PRODUCT_ID, PRODUCT_NAME, PRODUCT_CODE,
                        new BigDecimal("-1"), BigDecimal.TEN, List.of()));
    }

    @Test
    void should_AllowZeroUnitCost() {
        PurchaseLine line = PurchaseLine.create(PRODUCT_ID, PRODUCT_NAME, PRODUCT_CODE,
                BigDecimal.ONE, BigDecimal.ZERO, List.of());

        assertEquals(BigDecimal.ZERO, line.getUnitCost());
        assertEquals(BigDecimal.ZERO, line.getLineTotal());
    }

    @Test
    void should_Reconstitute_When_AllFieldsProvided() {
        PurchaseLineId id = PurchaseLineId.generate();

        PurchaseLine line = PurchaseLine.reconstitute(id, PRODUCT_ID, PRODUCT_NAME, PRODUCT_CODE,
                new BigDecimal("5"), new BigDecimal("2.00"), BigDecimal.ZERO, List.of());

        assertEquals(id, line.getId());
        assertEquals(PRODUCT_ID, line.getProductId());
        assertEquals(new BigDecimal("10.00"), line.getLineTotal());
    }

    @Test
    void should_ReturnUnmodifiableAllocations_When_GetAllocationsCalled() {
        PurchaseLineAllocation allocation = PurchaseLineAllocation.create(
                UUID.randomUUID(), BigDecimal.ONE);
        PurchaseLine line = PurchaseLine.create(PRODUCT_ID, PRODUCT_NAME, PRODUCT_CODE,
                BigDecimal.ONE, BigDecimal.TEN, List.of(allocation));

        assertThrows(UnsupportedOperationException.class, () ->
                line.getAllocations().add(allocation));
    }
}
