package com.cashly.cashly_api.finance.billing.invoices.application.ports.out;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface InvoicePaymentTotalsGateway {
    Map<UUID, BigDecimal> sumByInvoiceIds(List<UUID> invoiceIds);
}
