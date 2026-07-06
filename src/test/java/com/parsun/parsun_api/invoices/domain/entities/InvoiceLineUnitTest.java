package com.parsun.parsun_api.invoices.domain.entities;

import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceLineId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("InvoiceLine Domain Entity Unit Tests")
class InvoiceLineUnitTest {

    private static final UUID PRODUCT_ID = UUID.randomUUID();
    private static final String PRODUCT_NAME = "Producto A";
    private static final String PRODUCT_CODE = "PROD-001";

    @Test
    void should_CreateInvoiceLine_When_ValidDataProvided() {
        InvoiceLine line = InvoiceLine.create(PRODUCT_ID, PRODUCT_NAME, PRODUCT_CODE,
                new BigDecimal("2"), new BigDecimal("10.00"), List.of());

        assertNotNull(line.getId());
        assertEquals(PRODUCT_ID, line.getProductId());
        assertEquals(PRODUCT_NAME, line.getProductName());
        assertEquals(PRODUCT_CODE, line.getProductCode());
        assertEquals(new BigDecimal("2"), line.getQuantity());
        assertEquals(new BigDecimal("10.00"), line.getUnitPrice());
        assertEquals(new BigDecimal("20.00"), line.getLineTotal());
        assertTrue(line.getAllocations().isEmpty());
    }

    @Test
    void should_CalculateLineTotal_When_Created() {
        InvoiceLine line = InvoiceLine.create(PRODUCT_ID, PRODUCT_NAME, PRODUCT_CODE,
                new BigDecimal("3"), new BigDecimal("5.50"), List.of());

        assertEquals(new BigDecimal("16.50"), line.getLineTotal());
    }

    @Test
    void should_CalculateLineTotal_WithDecimalPrecision() {
        InvoiceLine line = InvoiceLine.create(PRODUCT_ID, PRODUCT_NAME, PRODUCT_CODE,
                new BigDecimal("1.5"), new BigDecimal("3.33"), List.of());

        assertEquals(new BigDecimal("4.995"), line.getLineTotal());
    }

    @Test
    void should_CreateWithAllocations_When_Provided() {
        InvoiceLineAllocation allocation = InvoiceLineAllocation.create(UUID.randomUUID(), BigDecimal.valueOf(5));

        InvoiceLine line = InvoiceLine.create(PRODUCT_ID, PRODUCT_NAME, PRODUCT_CODE,
                BigDecimal.ONE, BigDecimal.TEN, List.of(allocation));

        assertEquals(1, line.getAllocations().size());
        assertEquals(allocation.getWarehouseId(), line.getAllocations().get(0).getWarehouseId());
    }

    @Test
    void should_CreateWithEmptyAllocations_When_NullProvided() {
        InvoiceLine line = InvoiceLine.create(PRODUCT_ID, PRODUCT_NAME, PRODUCT_CODE,
                BigDecimal.ONE, BigDecimal.TEN, null);

        assertNotNull(line.getAllocations());
        assertTrue(line.getAllocations().isEmpty());
    }

    @Test
    void should_ThrowException_When_IdIsNull() {
        assertThrows(NullPointerException.class, () ->
                InvoiceLine.reconstitute(null, PRODUCT_ID, PRODUCT_NAME, PRODUCT_CODE,
                        BigDecimal.ONE, BigDecimal.TEN, BigDecimal.ZERO, List.of()));
    }

    @Test
    void should_ThrowException_When_ProductIdIsNull() {
        assertThrows(NullPointerException.class, () ->
                InvoiceLine.create(null, PRODUCT_NAME, PRODUCT_CODE,
                        BigDecimal.ONE, BigDecimal.TEN, List.of()));
    }

    @Test
    void should_ThrowException_When_ProductNameIsNull() {
        assertThrows(NullPointerException.class, () ->
                InvoiceLine.create(PRODUCT_ID, null, PRODUCT_CODE,
                        BigDecimal.ONE, BigDecimal.TEN, List.of()));
    }

    @Test
    void should_ThrowException_When_ProductCodeIsNull() {
        assertThrows(NullPointerException.class, () ->
                InvoiceLine.create(PRODUCT_ID, PRODUCT_NAME, null,
                        BigDecimal.ONE, BigDecimal.TEN, List.of()));
    }

    @Test
    void should_ThrowException_When_QuantityIsNull() {
        assertThrows(NullPointerException.class, () ->
                InvoiceLine.create(PRODUCT_ID, PRODUCT_NAME, PRODUCT_CODE,
                        null, BigDecimal.TEN, List.of()));
    }

    @Test
    void should_ThrowException_When_UnitPriceIsNull() {
        assertThrows(NullPointerException.class, () ->
                InvoiceLine.create(PRODUCT_ID, PRODUCT_NAME, PRODUCT_CODE,
                        BigDecimal.ONE, null, List.of()));
    }

    @Test
    void should_ThrowException_When_QuantityIsZero() {
        assertThrows(IllegalArgumentException.class, () ->
                InvoiceLine.create(PRODUCT_ID, PRODUCT_NAME, PRODUCT_CODE,
                        BigDecimal.ZERO, BigDecimal.TEN, List.of()));
    }

    @Test
    void should_ThrowException_When_QuantityIsNegative() {
        assertThrows(IllegalArgumentException.class, () ->
                InvoiceLine.create(PRODUCT_ID, PRODUCT_NAME, PRODUCT_CODE,
                        new BigDecimal("-1"), BigDecimal.TEN, List.of()));
    }

    @Test
    void should_AllowZeroUnitPrice() {
        InvoiceLine line = InvoiceLine.create(PRODUCT_ID, PRODUCT_NAME, PRODUCT_CODE,
                BigDecimal.ONE, BigDecimal.ZERO, List.of());

        assertEquals(BigDecimal.ZERO, line.getUnitPrice());
        assertEquals(BigDecimal.ZERO, line.getLineTotal());
    }

    @Test
    void should_Reconstitute_When_AllFieldsProvided() {
        InvoiceLineId id = InvoiceLineId.generate();

        InvoiceLine line = InvoiceLine.reconstitute(id, PRODUCT_ID, PRODUCT_NAME, PRODUCT_CODE,
                new BigDecimal("5"), new BigDecimal("2.00"), BigDecimal.ZERO, List.of());

        assertEquals(id, line.getId());
        assertEquals(PRODUCT_ID, line.getProductId());
        assertEquals(new BigDecimal("10.00"), line.getLineTotal());
    }

    @Test
    void should_ReturnUnmodifiableAllocations_When_GetAllocationsCalled() {
        InvoiceLineAllocation allocation = InvoiceLineAllocation.create(UUID.randomUUID(), BigDecimal.ONE);
        InvoiceLine line = InvoiceLine.create(PRODUCT_ID, PRODUCT_NAME, PRODUCT_CODE,
                BigDecimal.ONE, BigDecimal.TEN, List.of(allocation));

        assertThrows(UnsupportedOperationException.class, () ->
                line.getAllocations().add(allocation));
    }


    // ============================================================
    // decreaseReturnedQuantity()
    // ============================================================

    @Test
    void should_DecreaseReturnedQuantity_When_ValidQuantityProvided() {
        InvoiceLine line = InvoiceLine.reconstitute(
                InvoiceLineId.generate(), PRODUCT_ID, PRODUCT_NAME, PRODUCT_CODE,
                new BigDecimal("10"), new BigDecimal("10.00"), new BigDecimal("5"), List.of());

        line.decreaseReturnedQuantity(new BigDecimal("3"));

        assertEquals(new BigDecimal("2"), line.getReturnedQuantity());
    }

    @Test
    void should_ThrowException_When_DecreaseQuantityIsNull() {
        InvoiceLine line = InvoiceLine.reconstitute(
                InvoiceLineId.generate(), PRODUCT_ID, PRODUCT_NAME, PRODUCT_CODE,
                new BigDecimal("10"), new BigDecimal("10.00"), new BigDecimal("5"), List.of());

        assertThrows(NullPointerException.class, () ->
                line.decreaseReturnedQuantity(null));
    }

    @Test
    void should_ThrowException_When_DecreaseQuantityIsNegative() {
        InvoiceLine line = InvoiceLine.reconstitute(
                InvoiceLineId.generate(), PRODUCT_ID, PRODUCT_NAME, PRODUCT_CODE,
                new BigDecimal("10"), new BigDecimal("10.00"), new BigDecimal("5"), List.of());

        assertThrows(IllegalArgumentException.class, () ->
                line.decreaseReturnedQuantity(new BigDecimal("-1")));
    }

    @Test
    void should_ThrowException_When_DecreaseQuantityWouldMakeReturnedNegative() {
        InvoiceLine line = InvoiceLine.reconstitute(
                InvoiceLineId.generate(), PRODUCT_ID, PRODUCT_NAME, PRODUCT_CODE,
                new BigDecimal("10"), new BigDecimal("10.00"), new BigDecimal("5"), List.of());

        assertThrows(IllegalArgumentException.class, () ->
                line.decreaseReturnedQuantity(new BigDecimal("6")));
    }

    @Test
    void should_AllowDecreasingReturnedQuantityToExactlyZero() {
        InvoiceLine line = InvoiceLine.reconstitute(
                InvoiceLineId.generate(), PRODUCT_ID, PRODUCT_NAME, PRODUCT_CODE,
                new BigDecimal("10"), new BigDecimal("10.00"), new BigDecimal("5"), List.of());

        line.decreaseReturnedQuantity(new BigDecimal("5"));

        assertEquals(BigDecimal.ZERO, line.getReturnedQuantity());
    }
}
