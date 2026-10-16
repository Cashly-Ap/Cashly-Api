package com.cashly.cashly_api.payments.collection.infrastructure.integration;

import com.cashly.cashly_api.payments.collection.application.ports.in.InvoicePaymentTotalQuery;
import com.cashly.cashly_api.payments.collection.domain.repository.PaymentAllocationRepository;
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
