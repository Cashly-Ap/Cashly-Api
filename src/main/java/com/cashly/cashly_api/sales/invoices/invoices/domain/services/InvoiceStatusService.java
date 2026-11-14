package com.cashly.cashly_api.sales.invoices.invoices.domain.services;

import java.util.UUID;

public interface InvoiceStatusService {
    void recalculate(UUID invoiceId);
}
