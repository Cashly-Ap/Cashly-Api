package com.cashly.cashly_api.invoices.infrastructure.integration;

import com.cashly.cashly_api.invoices.application.contracts.InvoicePaymentTotalsGateway;
import com.cashly.cashly_api.payments.application.api.InvoicePaymentTotalsQuery;
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
