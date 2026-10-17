package com.cashly.cashly_api.finance.billing.invoices.domain.services;

import java.util.UUID;

public interface InvoiceStatusService {
    void recalculate(UUID invoiceId);
}
