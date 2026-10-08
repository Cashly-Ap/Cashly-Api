package com.cashly.cashly_api.payments.application.api;

import java.math.BigDecimal;
import java.util.UUID;

public interface InvoicePaymentTotalQuery {
    BigDecimal totalAllocatedByInvoiceId(UUID invoiceId);
}
