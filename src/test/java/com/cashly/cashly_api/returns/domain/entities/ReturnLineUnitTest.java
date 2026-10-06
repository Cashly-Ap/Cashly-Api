package com.cashly.cashly_api.returns.domain.entities;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ReturnLine Value Object Unit Tests")
class ReturnLineUnitTest {

    private static final UUID INVOICE_LINE_ID = UUID.randomUUID();
    private static final UUID PRODUCT_ID = UUID.randomUUID();
    private static final BigDecimal QUANTITY = BigDecimal.ONE;
    private static final BigDecimal UNIT_PRICE = new BigDecimal("50.00");
    private static final BigDecimal SUBTOTAL = new BigDecimal("50.00");

    @Test
    void should_CreateReturnLine_When_ValidData() {
        ReturnLine line = ReturnLine.create(
                INVOICE_LINE_ID, PRODUCT_ID, QUANTITY, UNIT_PRICE, SUBTOTAL, List.of()
        );

        assertEquals(INVOICE_LINE_ID, line.getInvoiceLineId());
        assertEquals(PRODUCT_ID, line.getProductId());
        assertEquals(QUANTITY, line.getQuantity());
        assertEquals(UNIT_PRICE, line.getUnitPrice());
        assertEquals(SUBTOTAL, line.getSubtotal());
        assertTrue(line.getAllocations().isEmpty());
    }

    @Test
    void should_CreateReturnLine_When_WithAllocations() {
        ReturnLineAllocation allocation = ReturnLineAllocation.create(
                UUID.randomUUID(), BigDecimal.ONE
        );
        ReturnLine line = ReturnLine.create(
                INVOICE_LINE_ID, PRODUCT_ID, QUANTITY, UNIT_PRICE, SUBTOTAL,
                List.of(allocation)
        );

        assertEquals(1, line.getAllocations().size());
        assertEquals(allocation.getAllocated(), line.getAllocations().get(0).getAllocated());
    }

    @Test
    void should_ReturnEmptyAllocations_When_NullAllocations() {
        ReturnLine line = ReturnLine.create(
                INVOICE_LINE_ID, PRODUCT_ID, QUANTITY, UNIT_PRICE, SUBTOTAL, null
        );

        assertTrue(line.getAllocations().isEmpty());
    }

    @Test
    void should_ThrowException_When_InvoiceLineIdIsNull() {
        assertThrows(NullPointerException.class, () ->
                ReturnLine.create(null, PRODUCT_ID, QUANTITY, UNIT_PRICE, SUBTOTAL, List.of())
        );
    }

    @Test
    void should_ThrowException_When_ProductIdIsNull() {
        assertThrows(NullPointerException.class, () ->
                ReturnLine.create(INVOICE_LINE_ID, null, QUANTITY, UNIT_PRICE, SUBTOTAL, List.of())
        );
    }

    @Test
    void should_ThrowException_When_QuantityIsNull() {
        assertThrows(NullPointerException.class, () ->
                ReturnLine.create(INVOICE_LINE_ID, PRODUCT_ID, null, UNIT_PRICE, SUBTOTAL, List.of())
        );
    }

    @Test
    void should_ThrowException_When_QuantityIsZero() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                ReturnLine.create(INVOICE_LINE_ID, PRODUCT_ID, BigDecimal.ZERO, UNIT_PRICE, SUBTOTAL, List.of())
        );

        assertEquals("Quantity must be greater than zero", exception.getMessage());
    }

    @Test
    void should_ThrowException_When_QuantityIsNegative() {
        assertThrows(IllegalArgumentException.class, () ->
                ReturnLine.create(INVOICE_LINE_ID, PRODUCT_ID, new BigDecimal("-1"), UNIT_PRICE, SUBTOTAL, List.of())
        );
    }

    @Test
    void should_ThrowException_When_UnitPriceIsNull() {
        assertThrows(NullPointerException.class, () ->
                ReturnLine.create(INVOICE_LINE_ID, PRODUCT_ID, QUANTITY, null, SUBTOTAL, List.of())
        );
    }

    @Test
    void should_ThrowException_When_UnitPriceIsNegative() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                ReturnLine.create(INVOICE_LINE_ID, PRODUCT_ID, QUANTITY, new BigDecimal("-1"), SUBTOTAL, List.of())
        );

        assertEquals("Unit price must be greater than or equal to zero", exception.getMessage());
    }

    @Test
    void should_AllowUnitPriceOfZero() {
        ReturnLine line = ReturnLine.create(
                INVOICE_LINE_ID, PRODUCT_ID, QUANTITY, BigDecimal.ZERO, BigDecimal.ZERO, List.of()
        );

        assertEquals(BigDecimal.ZERO, line.getUnitPrice());
    }

    @Test
    void should_ThrowException_When_SubtotalIsNull() {
        assertThrows(NullPointerException.class, () ->
                ReturnLine.create(INVOICE_LINE_ID, PRODUCT_ID, QUANTITY, UNIT_PRICE, null, List.of())
        );
    }

    @Test
    void should_BeEqual_When_SameValues() {
        ReturnLine line1 = ReturnLine.create(
                INVOICE_LINE_ID, PRODUCT_ID, QUANTITY, UNIT_PRICE, SUBTOTAL, List.of()
        );
        ReturnLine line2 = ReturnLine.create(
                UUID.randomUUID(), PRODUCT_ID, QUANTITY, UNIT_PRICE, SUBTOTAL, List.of()
        );

        assertEquals(line1, line2);
        assertEquals(line1.hashCode(), line2.hashCode());
    }

    @Test
    void should_NotBeEqual_When_DifferentProduct() {
        ReturnLine line1 = ReturnLine.create(
                INVOICE_LINE_ID, PRODUCT_ID, QUANTITY, UNIT_PRICE, SUBTOTAL, List.of()
        );
        ReturnLine line2 = ReturnLine.create(
                INVOICE_LINE_ID, UUID.randomUUID(), QUANTITY, UNIT_PRICE, SUBTOTAL, List.of()
        );

        assertNotEquals(line1, line2);
    }
}
