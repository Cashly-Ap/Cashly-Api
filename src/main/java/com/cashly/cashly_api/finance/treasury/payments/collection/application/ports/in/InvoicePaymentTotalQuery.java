package com.cashly.cashly_api.finance.treasury.payments.collection.application.ports.in;

import java.math.BigDecimal;
import java.util.UUID;

public interface InvoicePaymentTotalQuery {
    BigDecimal totalAllocatedByInvoiceId(UUID invoiceId);
}
