package com.cashly.cashly_api.invoices.reporting.application.generate_invoice_pdf;

import java.util.UUID;

public interface GenerateInvoicePdfUseCase {
    byte[] execute(UUID invoiceId);
}
