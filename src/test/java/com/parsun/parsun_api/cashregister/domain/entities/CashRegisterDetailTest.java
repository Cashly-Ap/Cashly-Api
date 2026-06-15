package com.parsun.parsun_api.cashregister.domain.entities;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("CashRegisterDetail Domain Entity Unit Tests")
class CashRegisterDetailTest {

    private final UUID paymentMethodId = UUID.randomUUID();
    private final String paymentMethodName = "Efectivo";
    private final int transactionCount = 5;
    private final BigDecimal expectedAmount = new BigDecimal("1000.00");

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        void should_CreateDetail_When_ValidPositiveAmount() {
            CashRegisterDetail detail = CashRegisterDetail.create(
                    paymentMethodId, paymentMethodName, transactionCount, expectedAmount);

            assertNotNull(detail.getId());
            assertEquals(paymentMethodId, detail.getPaymentMethodId());
            assertEquals(paymentMethodName, detail.getPaymentMethodName());
            assertEquals(transactionCount, detail.getTransactionCount());
            assertEquals(expectedAmount, detail.getExpectedAmount());
        }

        @Test
        void should_CreateDetail_When_AmountIsZero() {
            CashRegisterDetail detail = CashRegisterDetail.create(
                    paymentMethodId, paymentMethodName, 0, BigDecimal.ZERO);

            assertEquals(BigDecimal.ZERO, detail.getExpectedAmount());
            assertEquals(0, detail.getTransactionCount());
        }

        @Test
        void should_Throw_When_AmountIsNegative() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    CashRegisterDetail.create(paymentMethodId, paymentMethodName, transactionCount,
                            new BigDecimal("-100.00")));

            assertEquals("Expected amount must not be negative", exception.getMessage());
        }

        @Test
        void should_Throw_When_PaymentMethodNameIsNull() {
            assertThrows(NullPointerException.class, () ->
                    CashRegisterDetail.create(paymentMethodId, null, transactionCount, expectedAmount));
        }

        @Test
        void should_Throw_When_ExpectedAmountIsNull() {
            assertThrows(NullPointerException.class, () ->
                    CashRegisterDetail.create(paymentMethodId, paymentMethodName, transactionCount, null));
        }
    }

    @Nested
    @DisplayName("reconstitute()")
    class Reconstitute {

        @Test
        void should_Reconstitute_When_AllFieldsProvided() {
            UUID id = UUID.randomUUID();

            CashRegisterDetail detail = CashRegisterDetail.reconstitute(
                    id, paymentMethodId, paymentMethodName, transactionCount, expectedAmount);

            assertEquals(id, detail.getId());
            assertEquals(paymentMethodId, detail.getPaymentMethodId());
            assertEquals(paymentMethodName, detail.getPaymentMethodName());
            assertEquals(transactionCount, detail.getTransactionCount());
            assertEquals(expectedAmount, detail.getExpectedAmount());
        }

        @Test
        void should_ReconstituteWithNegativeAmount_When_AmountIsNegative() {
            CashRegisterDetail detail = CashRegisterDetail.reconstitute(
                    UUID.randomUUID(), paymentMethodId, paymentMethodName, 0, new BigDecimal("-50.00"));

            assertEquals(new BigDecimal("-50.00"), detail.getExpectedAmount());
        }
    }

    @Nested
    @DisplayName("Getters")
    class Getters {

        @Test
        void should_ReturnAllFields_ThroughGetters() {
            CashRegisterDetail detail = CashRegisterDetail.create(
                    paymentMethodId, paymentMethodName, transactionCount, expectedAmount);

            assertNotNull(detail.getId());
            assertNotNull(detail.getPaymentMethodId());
            assertNotNull(detail.getPaymentMethodName());
            assertEquals(transactionCount, detail.getTransactionCount());
            assertNotNull(detail.getExpectedAmount());
        }
    }
}
