package com.cashly.cashly_api.sales.invoices.reporting.application.generate_invoice_pdf.use_case;

import java.util.UUID;

public interface GenerateInvoicePdfUseCase {
    byte[] execute(UUID invoiceId);
}
