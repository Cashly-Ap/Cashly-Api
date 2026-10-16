package com.cashly.cashly_api.invoices.domain.services;

import java.util.UUID;

public interface InvoiceStatusService {
    void recalculate(UUID invoiceId);
}
