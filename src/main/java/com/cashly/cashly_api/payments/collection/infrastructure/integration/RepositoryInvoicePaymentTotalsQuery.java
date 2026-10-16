package com.cashly.cashly_api.payments.collection.infrastructure.integration;

import com.cashly.cashly_api.payments.collection.application.ports.in.InvoicePaymentTotalsQuery;
import com.cashly.cashly_api.payments.collection.domain.PaymentAllocationRepository;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

@Component
public class RepositoryInvoicePaymentTotalsQuery implements InvoicePaymentTotalsQuery {
    private final PaymentAllocationRepository allocations;

    public RepositoryInvoicePaymentTotalsQuery(PaymentAllocationRepository allocations) {
        this.allocations = allocations;
    }

    @Override
    public Map<UUID, java.math.BigDecimal> sumByInvoiceIds(Collection<UUID> invoiceIds) {
        return invoiceIds.isEmpty() ? Map.of() : allocations.sumByInvoiceIds(invoiceIds);
    }
}
