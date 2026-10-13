package com.cashly.cashly_api.invoices.application.ports.out;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface InvoicePaymentTotalsGateway {
    Map<UUID, BigDecimal> sumByInvoiceIds(List<UUID> invoiceIds);
}
