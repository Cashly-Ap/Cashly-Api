package com.cashly.cashly_api.invoices.infrastructure.adapters.out.integration;

import com.cashly.cashly_api.invoices.application.ports.out.InvoicePaymentTotalsGateway;
import com.cashly.cashly_api.payments.application.ports.in.InvoicePaymentTotalsQuery;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class LocalInvoicePaymentTotalsAdapter implements InvoicePaymentTotalsGateway {
    private final InvoicePaymentTotalsQuery payments;

    public LocalInvoicePaymentTotalsAdapter(InvoicePaymentTotalsQuery payments) {
        this.payments = payments;
    }

    @Override
    public Map<UUID, BigDecimal> sumByInvoiceIds(List<UUID> invoiceIds) {
        return payments.sumByInvoiceIds(invoiceIds);
    }
}
