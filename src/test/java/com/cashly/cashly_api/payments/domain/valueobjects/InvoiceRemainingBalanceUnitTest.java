package com.cashly.cashly_api.payments.domain.valueobjects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("InvoiceRemainingBalance Value Object Unit Tests")
class InvoiceRemainingBalanceUnitTest {

    @Test
    void should_CreateInvoiceRemainingBalance_When_ValidDataProvided() {
        UUID invoiceId = UUID.randomUUID();
        BigDecimal remainingAmount = new BigDecimal("300.00");

        InvoiceRemainingBalance balance = new InvoiceRemainingBalance(invoiceId, remainingAmount);

        assertEquals(invoiceId, balance.invoiceId());
        assertEquals(remainingAmount, balance.remainingAmount());
    }

    @Test
    void should_ThrowException_When_InvoiceIdIsNull() {
        assertThrows(NullPointerException.class,
                () -> new InvoiceRemainingBalance(null, BigDecimal.TEN));
    }

    @Test
    void should_ThrowException_When_RemainingAmountIsNull() {
        assertThrows(NullPointerException.class,
                () -> new InvoiceRemainingBalance(UUID.randomUUID(), null));
    }
}
