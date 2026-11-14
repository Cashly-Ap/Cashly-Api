package com.cashly.cashly_api.sales.invoices.invoices.application.ports.in;

import java.util.List;
import java.util.UUID;

public interface InvoiceDocumentQuery {
    InvoiceDocumentSnapshot find(UUID invoiceId);
    List<InvoiceDocumentRow> findAll(InvoiceDocumentFilter filter);
}
