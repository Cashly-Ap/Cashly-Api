package com.parsun.parsun_api.invoices.domain.services;

import java.util.UUID;

public interface InvoiceStatusService {
    void recalculate(UUID invoiceId);
}
