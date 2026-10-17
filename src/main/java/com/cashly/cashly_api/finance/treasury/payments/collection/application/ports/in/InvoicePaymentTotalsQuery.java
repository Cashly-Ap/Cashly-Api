package com.cashly.cashly_api.finance.treasury.payments.collection.application.ports.in;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public interface InvoicePaymentTotalsQuery {
    Map<UUID, BigDecimal> sumByInvoiceIds(Collection<UUID> invoiceIds);
}
