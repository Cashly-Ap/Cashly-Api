package com.cashly.cashly_api.finance.treasury.payments.collection.infrastructure.integration;

import com.cashly.cashly_api.finance.treasury.payments.collection.domain.entity.PaymentAllocation;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.repository.PaymentAllocationRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class RepositoryInvoicePaymentTotalQueryTest {
    @Test
    void givenAllocations_whenQueryingTotal_thenSumsNullAmountsAsZero() {
        PaymentAllocationRepository allocations = mock(
                PaymentAllocationRepository.class);
        PaymentAllocation first = mock(PaymentAllocation.class);
        PaymentAllocation second = mock(PaymentAllocation.class);
        UUID invoiceId = UUID.randomUUID();
        when(first.getAllocatedAmount()).thenReturn(new BigDecimal("12.00"));
        when(second.getAllocatedAmount()).thenReturn(null);
        when(allocations.findByInvoiceId(invoiceId))
                .thenReturn(List.of(first, second));

        BigDecimal total = new RepositoryInvoicePaymentTotalQuery(allocations)
                .totalAllocatedByInvoiceId(invoiceId);

        assertEquals(new BigDecimal("12.00"), total);
    }
}
