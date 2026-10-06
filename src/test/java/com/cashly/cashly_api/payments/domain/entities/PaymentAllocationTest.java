package com.cashly.cashly_api.payments.domain.entities;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("PaymentAllocation Domain Entity Unit Tests")
class PaymentAllocationTest {

    private final UUID paymentId = UUID.randomUUID();
    private final UUID invoiceId = UUID.randomUUID();
    private final BigDecimal allocatedAmount = new BigDecimal("500.00");

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        void should_CreateAllocation_When_ValidData() {
            PaymentAllocation allocation = PaymentAllocation.create(paymentId, invoiceId, allocatedAmount);

            assertNotNull(allocation.getId());
            assertEquals(paymentId, allocation.getPaymentId());
            assertEquals(invoiceId, allocation.getInvoiceId());
            assertEquals(allocatedAmount, allocation.getAllocatedAmount());
            assertNotNull(allocation.getCreatedAt());
        }

        @Test
        void should_Throw_When_PaymentIdIsNull() {
            assertThrows(NullPointerException.class, () ->
                    PaymentAllocation.create(null, invoiceId, allocatedAmount));
        }

        @Test
        void should_Throw_When_InvoiceIdIsNull() {
            assertThrows(NullPointerException.class, () ->
                    PaymentAllocation.create(paymentId, null, allocatedAmount));
        }

        @Test
        void should_Throw_When_AllocatedAmountIsNull() {
            assertThrows(NullPointerException.class, () ->
                    PaymentAllocation.create(paymentId, invoiceId, null));
        }

        @Test
        void should_Throw_When_AllocatedAmountIsZero() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    PaymentAllocation.create(paymentId, invoiceId, BigDecimal.ZERO));

            assertEquals("Allocated amount must be greater than zero", exception.getMessage());
        }

        @Test
        void should_Throw_When_AllocatedAmountIsNegative() {
            assertThrows(IllegalArgumentException.class, () ->
                    PaymentAllocation.create(paymentId, invoiceId, new BigDecimal("-100.00")));
        }
    }

    @Nested
    @DisplayName("reconstitute()")
    class Reconstitute {

        @Test
        void should_Reconstitute_When_AllFieldsProvided() {
            UUID id = UUID.randomUUID();
            LocalDateTime createdAt = LocalDateTime.now();

            PaymentAllocation allocation = PaymentAllocation.reconstitute(
                    id, paymentId, invoiceId, allocatedAmount, createdAt);

            assertEquals(id, allocation.getId());
            assertEquals(paymentId, allocation.getPaymentId());
            assertEquals(invoiceId, allocation.getInvoiceId());
            assertEquals(allocatedAmount, allocation.getAllocatedAmount());
            assertEquals(createdAt, allocation.getCreatedAt());
        }

        @Test
        void should_ReconstituteWithZeroAmount_When_AmountIsZero() {
            PaymentAllocation allocation = PaymentAllocation.reconstitute(
                    UUID.randomUUID(), paymentId, invoiceId, BigDecimal.ZERO, LocalDateTime.now());

            assertEquals(BigDecimal.ZERO, allocation.getAllocatedAmount());
        }
    }

    @Nested
    @DisplayName("Getters")
    class Getters {

        @Test
        void should_ReturnAllFields_ThroughGetters() {
            PaymentAllocation allocation = PaymentAllocation.create(paymentId, invoiceId, allocatedAmount);

            assertNotNull(allocation.getId());
            assertNotNull(allocation.getPaymentId());
            assertNotNull(allocation.getInvoiceId());
            assertNotNull(allocation.getAllocatedAmount());
            assertNotNull(allocation.getCreatedAt());
        }
    }
}
