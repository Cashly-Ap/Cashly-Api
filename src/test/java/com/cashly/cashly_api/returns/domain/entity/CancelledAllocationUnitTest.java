package com.cashly.cashly_api.returns.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CancelledAllocation Domain Entity Unit Tests")
class CancelledAllocationUnitTest {

    private static final UUID PAYMENT_ID = UUID.randomUUID();
    private static final UUID INVOICE_ID = UUID.randomUUID();
    private static final BigDecimal AMOUNT = new BigDecimal("500.00");

    @Test
    void should_CreateCancelledAllocation_When_ValidParametersProvided() {
        CancelledAllocation allocation = CancelledAllocation.create(PAYMENT_ID, INVOICE_ID, AMOUNT);

        assertNotNull(allocation);
        assertEquals(PAYMENT_ID, allocation.getPaymentId());
        assertEquals(INVOICE_ID, allocation.getInvoiceId());
        assertEquals(AMOUNT, allocation.getAmount());
    }

    @Test
    void should_Reconstitute_When_AllFieldsProvided() {
        CancelledAllocation allocation = CancelledAllocation.reconstitute(PAYMENT_ID, INVOICE_ID, AMOUNT);

        assertNotNull(allocation);
        assertEquals(PAYMENT_ID, allocation.getPaymentId());
        assertEquals(INVOICE_ID, allocation.getInvoiceId());
        assertEquals(AMOUNT, allocation.getAmount());
    }

    @Test
    void should_ThrowException_When_PaymentIdIsNull() {
        assertThrows(NullPointerException.class, () ->
                CancelledAllocation.create(null, INVOICE_ID, AMOUNT));
    }

    @Test
    void should_ThrowException_When_InvoiceIdIsNull() {
        assertThrows(NullPointerException.class, () ->
                CancelledAllocation.create(PAYMENT_ID, null, AMOUNT));
    }

    @Test
    void should_ThrowException_When_AmountIsNull() {
        assertThrows(NullPointerException.class, () ->
                CancelledAllocation.create(PAYMENT_ID, INVOICE_ID, null));
    }

    @Test
    void should_ThrowException_When_AmountIsZero() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                CancelledAllocation.create(PAYMENT_ID, INVOICE_ID, BigDecimal.ZERO));

        assertEquals("Amount must be greater than zero", exception.getMessage());
    }

    @Test
    void should_ThrowException_When_AmountIsNegative() {
        assertThrows(IllegalArgumentException.class, () ->
                CancelledAllocation.create(PAYMENT_ID, INVOICE_ID, new BigDecimal("-100.00")));
    }

    @Test
    void should_BeEqual_When_SamePaymentIdAndInvoiceId() {
        CancelledAllocation allocation1 = CancelledAllocation.create(PAYMENT_ID, INVOICE_ID, AMOUNT);
        CancelledAllocation allocation2 = CancelledAllocation.reconstitute(PAYMENT_ID, INVOICE_ID, AMOUNT);

        assertEquals(allocation1, allocation2);
        assertEquals(allocation1.hashCode(), allocation2.hashCode());
    }

    @Test
    void should_NotBeEqual_When_DifferentPaymentId() {
        CancelledAllocation allocation1 = CancelledAllocation.create(PAYMENT_ID, INVOICE_ID, AMOUNT);
        CancelledAllocation allocation2 = CancelledAllocation.create(
                UUID.randomUUID(), INVOICE_ID, AMOUNT);

        assertNotEquals(allocation1, allocation2);
    }

    @Test
    void should_NotBeEqual_When_DifferentInvoiceId() {
        CancelledAllocation allocation1 = CancelledAllocation.create(PAYMENT_ID, INVOICE_ID, AMOUNT);
        CancelledAllocation allocation2 = CancelledAllocation.create(
                PAYMENT_ID, UUID.randomUUID(), AMOUNT);

        assertNotEquals(allocation1, allocation2);
    }

    @Test
    void should_NotBeEqual_When_Null() {
        CancelledAllocation allocation = CancelledAllocation.create(PAYMENT_ID, INVOICE_ID, AMOUNT);

        assertNotEquals(null, allocation);
    }

    @Test
    void should_NotBeEqual_When_DifferentClass() {
        CancelledAllocation allocation = CancelledAllocation.create(PAYMENT_ID, INVOICE_ID, AMOUNT);

        assertNotEquals("some string", allocation);
    }

    @Test
    void should_BeEqual_When_SameReference() {
        CancelledAllocation allocation = CancelledAllocation.create(PAYMENT_ID, INVOICE_ID, AMOUNT);

        assertEquals(allocation, allocation);
    }

    @Test
    void should_ReturnString_When_ToStringCalled() {
        CancelledAllocation allocation = CancelledAllocation.create(PAYMENT_ID, INVOICE_ID, AMOUNT);

        String result = allocation.toString();

        assertTrue(result.contains(PAYMENT_ID.toString()));
        assertTrue(result.contains(INVOICE_ID.toString()));
        assertTrue(result.contains(AMOUNT.toString()));
    }
}
