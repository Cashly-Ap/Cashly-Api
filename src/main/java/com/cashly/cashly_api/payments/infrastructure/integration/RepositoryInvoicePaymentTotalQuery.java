package com.cashly.cashly_api.payments.infrastructure.integration;

import com.cashly.cashly_api.payments.application.api.InvoicePaymentTotalQuery;
import com.cashly.cashly_api.payments.domain.repositories.PaymentAllocationRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class RepositoryInvoicePaymentTotalQuery implements InvoicePaymentTotalQuery {
    private final PaymentAllocationRepository allocations;

    public RepositoryInvoicePaymentTotalQuery(PaymentAllocationRepository allocations) {
        this.allocations = allocations;
    }

    @Override
    public BigDecimal totalAllocatedByInvoiceId(UUID invoiceId) {
        return allocations.findByInvoiceId(invoiceId).stream()
                .map(allocation -> allocation.getAllocatedAmount() == null
                        ? BigDecimal.ZERO : allocation.getAllocatedAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
