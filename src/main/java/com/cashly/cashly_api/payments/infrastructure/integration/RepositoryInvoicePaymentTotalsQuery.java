package com.cashly.cashly_api.payments.infrastructure.integration;

import com.cashly.cashly_api.payments.application.api.InvoicePaymentTotalsQuery;
import com.cashly.cashly_api.payments.domain.repositories.PaymentAllocationRepository;
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
